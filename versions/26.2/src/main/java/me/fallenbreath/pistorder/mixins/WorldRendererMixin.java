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

package me.fallenbreath.pistorder.mixins;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.pistorder.impl.BatchedTextDrawer;
import me.fallenbreath.pistorder.impl.Pistorder;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin
{
	@Shadow @Final
	private LevelTargetBundle targets;

	@Shadow
	private void checkPoseStack(PoseStack poseStack)
	{
	}

	@Inject(
			method = "addAlwaysOnTopPass",
			at = @At("HEAD")
	)
	private void addPistorderLateDebugPass(
			FrameGraphBuilder frame,
			FeatureRenderDispatcher.PreparedFrame featureFrame,
			GpuBufferSlice fog,
			CallbackInfo ci
	)
	{
		FramePass pass = frame.addPass("pistorder_late_debug");
		this.targets.main = pass.readsAndWrites(this.targets.main);
		if (this.targets.itemEntity != null)
		{
			this.targets.itemEntity = pass.readsAndWrites(this.targets.itemEntity);
		}

		ResourceHandle<RenderTarget> mainTarget = this.targets.main;
		pass.executes(() -> {
			RenderSystem.setShaderFog(fog);
			PoseStack poseStack = new PoseStack();
			RenderTarget mainRenderTarget = mainTarget.get();
			RenderSystem.outputColorTextureOverride = mainRenderTarget.getColorTextureView();
			RenderSystem.outputDepthTextureOverride = mainRenderTarget.getDepthTextureView();
			try
			{
				try (BatchedTextDrawer drawer = BatchedTextDrawer.beginBatch())
				{
					Pistorder.getInstance().render(poseStack, 0);
					this.checkPoseStack(poseStack);
					drawer.draw();
				}
			}
			finally
			{
				RenderSystem.outputColorTextureOverride = null;
				RenderSystem.outputDepthTextureOverride = null;
			}
		});
	}
}
