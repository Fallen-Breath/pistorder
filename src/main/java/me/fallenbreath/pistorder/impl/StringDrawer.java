/*
 * This file is part of the Pistorder project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
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

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

//#if MC >= 11500
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;
//#if MC < 11904
//$$ import net.minecraft.util.math.AffineTransformation;
//#endif

//#else  // if MC >= 11500
//$$ import com.mojang.blaze3d.platform.GlStateManager;
//$$ import net.minecraft.client.render.entity.EntityRenderDispatcher;
//#endif

public class StringDrawer
{
	private static final double MAX_RENDER_DISTANCE = 256.0D;
	private static final float FONT_SIZE = 0.025F;

	/**
	 * Stolen from {@link DebugRenderer#drawString(MatrixStack, VertexConsumerProvider, String, double, double, double, int, float, boolean, float, boolean)}
	 */
	//#if 11600 <= MC && MC < 11700
	//$$ @SuppressWarnings("deprecation")
	//#endif
	public static void drawString(MatrixStack matrixStack, BlockPos pos, float tickDelta, float line, String[] texts, int[] colors)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		Camera camera = client.gameRenderer.getCamera();
		if (camera.isReady() && client.getEntityRenderDispatcher().gameOptions != null && client.player != null)
		{
			double x = (double)pos.getX() + 0.5D;
			double y = (double)pos.getY() + 0.5D;
			double z = (double)pos.getZ() + 0.5D;
			if (client.player.squaredDistanceTo(x, y, z) > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE)
			{
				return;
			}
			double camX = camera.getPos().x;
			double camY = camera.getPos().y;
			double camZ = camera.getPos().z;

			// ========================== Prepare Matrix start ==========================

			//#if MC >= 11700
			matrixStack.push();
			matrixStack.translate((float)(x - camX), (float)(y - camY), (float)(z - camZ));

			//#if MC >= 11800

			matrixStack.multiplyPositionMatrix(
			//#else
			//$$ matrixStack.method_34425(
			//#endif

					//#if MC >= 11904
					new Matrix4f().rotation(
					//#else
					//$$ new Matrix4f(
					//#endif
							camera.getRotation()
					)
			);
			matrixStack.scale(-FONT_SIZE, -FONT_SIZE, 1);
			//#if MC < 11904
			//$$ RenderSystem.enableTexture();
			//#endif
			RenderSystem.disableDepthTest();  // visibleThroughObjects
			//#if MC < 11904
			//$$ RenderSystem.depthMask(true);
			//$$ RenderSystem.applyModelViewMatrix();
			//#endif

			//#elseif MC >= 11500
			//$$ // if MC >= 11800
			//$$
			//$$ RenderSystem.pushMatrix();
			//$$ RenderSystem.translatef((float)(x - camX), (float)(y - camY), (float)(z - camZ));
			//$$ RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
			//$$ RenderSystem.multMatrix(new Matrix4f(camera.getRotation()));
			//$$ RenderSystem.scalef(-FONT_SIZE, -FONT_SIZE, 1);
			//$$ RenderSystem.enableTexture();
			//$$ RenderSystem.disableDepthTest();  // visibleThroughObjects
			//$$ RenderSystem.depthMask(true);
			//$$ RenderSystem.enableAlphaTest();
			//$$
			//#else
			//$$
			//$$ GlStateManager.pushMatrix();
			//$$ GlStateManager.translatef((float)(x - camX), (float)(y - camY), (float)(z - camZ));
			//$$ GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
			//$$ GlStateManager.scalef(FONT_SIZE, -FONT_SIZE, FONT_SIZE);
			//$$ EntityRenderDispatcher entityRenderDispatcher = client.getEntityRenderManager();
			//$$ GlStateManager.rotatef(-entityRenderDispatcher.cameraYaw, 0.0F, 1.0F, 0.0F);
			//$$ GlStateManager.rotatef(-entityRenderDispatcher.cameraPitch, 1.0F, 0.0F, 0.0F);
			//$$ GlStateManager.enableTexture();
			//$$ GlStateManager.disableDepthTest();  // visibleThroughObjects
			//$$ GlStateManager.depthMask(true);
			//$$ GlStateManager.scalef(-1.0F, 1.0F, 1.0F);
			//$$
			//#endif  // elseif MC >= 11500

			// ========================== Prepare Matrix end ==========================

			float totalWidth = 0.0F;
			for (String text: texts)
			{
				totalWidth += client.textRenderer.getWidth(text);
			}

			float writtenWidth = 0.0F;
			for (int i = 0; i < texts.length; i++)
			{
				float renderX = -totalWidth * 0.5F + writtenWidth;
				float renderY = client.textRenderer.getWrappedLinesHeight(texts[i], Integer.MAX_VALUE) * (-0.5F + 1.25F * line);

				//#if MC >= 11500

				VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
				//#if MC >= 11904
				Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
				//#else
				//$$ Matrix4f positionMatrix = AffineTransformation.identity().getMatrix();
				//#endif

				//#endif  // if MC >= 11500

				//#if MC >= 11500
				client.textRenderer.draw(
						texts[i], renderX, renderY, colors[i],
						false, positionMatrix, immediate,
						//#if MC >= 11904
						TextRenderer.TextLayerType.SEE_THROUGH,
						//#else
						//$$ true,
						//#endif
						0, 0xF000F0
				);
				//#else
				//$$ client.textRenderer.draw(texts[i], renderX, renderY, colors[i]);
				//#endif

				//#if MC >= 11500
				immediate.draw();
				//#endif

				writtenWidth += client.textRenderer.getWidth(texts[i]);
			}

			// ========================== Restore Matrix start ==========================

			//#if MC >= 11700

			//#if MC < 11904
			//$$ RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			//#endif

			RenderSystem.enableDepthTest();
			matrixStack.pop();

			//#elseif MC >= 11500
			//$$ // if MC >= 11800
			//$$
			//$$ RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			//$$ RenderSystem.enableDepthTest();
			//$$ RenderSystem.popMatrix();
			//$$
			//#else
			//$$
			//$$ GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			//$$ GlStateManager.enableDepthTest();
			//$$ GlStateManager.popMatrix();
			//$$
			//#endif  // elseif MC >= 11500

			// ========================== Restore Matrix end ==========================
		}
	}
}
