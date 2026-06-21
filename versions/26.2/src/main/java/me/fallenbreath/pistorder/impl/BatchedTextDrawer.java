/*
 * This file is part of the Pistorder project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Fallen_Breath and contributors
 *
 * Pistorder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pistorder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pistorder.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.pistorder.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class BatchedTextDrawer implements AutoCloseable
{
	private static final ThreadLocal<BatchedTextDrawer> ACTIVE_DRAWER = new ThreadLocal<>();

	private final StagedVertexBuffer stagedBuffer = new StagedVertexBuffer(() -> "Pistorder TextRenderer", 65536);
	private final Map<RenderType, StagedVertexBuffer.Draw> draws = new LinkedHashMap<>();

	public BatchedTextDrawer()
	{
	}

	public static BatchedTextDrawer beginBatch()
	{
		BatchedTextDrawer drawer = new BatchedTextDrawer();
		ACTIVE_DRAWER.set(drawer);
		return drawer;
	}

	public static BatchedTextDrawer getActive()
	{
		return ACTIVE_DRAWER.get();
	}

	public void append(Matrix4fc pose, Font.DisplayMode displayMode, int lightCoords, Font.PreparedText preparedText)
	{
		preparedText.visit(new Font.GlyphVisitor()
		{
			@Override
			public void acceptRenderable(@NonNull TextRenderable renderable)
			{
				RenderType renderType = renderable.renderType(displayMode);
				StagedVertexBuffer.Draw draw = BatchedTextDrawer.this.draws.computeIfAbsent(renderType, BatchedTextDrawer.this::createDraw);
				renderable.render(pose, BatchedTextDrawer.this.stagedBuffer.getVertexBuilder(draw), lightCoords, false);
			}
		});
	}

	public void draw()
	{
		if (this.draws.isEmpty())
		{
			return;
		}

		this.stagedBuffer.upload();
		this.draws.forEach((renderType, draw) -> {
			StagedVertexBuffer.ExecuteInfo executeInfo = this.stagedBuffer.getExecuteInfo(draw);
			if (executeInfo != null)
			{
				renderType.prepare().drawFromBuffer(executeInfo);
			}
		});
		this.stagedBuffer.endDraw();
	}

	@Override
	public void close()
	{
		if (ACTIVE_DRAWER.get() == this)
		{
			ACTIVE_DRAWER.remove();
		}
		this.stagedBuffer.close();
	}

	private StagedVertexBuffer.Draw createDraw(RenderType renderType)
	{
		return this.stagedBuffer.appendDraw(
				renderType.format(),
				renderType.primitiveTopology(),
				renderType.sortOnUpload() ? RenderSystem.getProjectionType().vertexSorting() : null
		);
	}
}
