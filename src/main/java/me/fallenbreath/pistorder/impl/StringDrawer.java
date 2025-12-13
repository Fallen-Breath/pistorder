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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

//#if MC >= 1.21.11
//$$ import net.minecraft.network.chat.FormattedText;
//#endif

//#if MC >= 12105
//$$ import com.mojang.blaze3d.opengl.GlStateManager;
//#endif

//#if MC >= 11500
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
//#if MC < 11904
//$$ import com.mojang.math.Transformation;
//#endif

//#else  // if MC >= 11500
//$$ import com.mojang.blaze3d.platform.GlStateManager;
//$$ import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
//#endif

public class StringDrawer
{
	private static final double MAX_RENDER_DISTANCE = 256.0D;
	private static final float FONT_SIZE = 0.025F;

	//#if MC >= 11500
	private static MultiBufferSource.BufferSource getVertexConsumer()
	{
		//#if MC >= 12100
		//$$ return Minecraft.getInstance().renderBuffers().bufferSource();
		//#else
		return MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		//#endif
	}
	//#endif

	/**
	 * Stolen from {@link DebugRenderer#renderFloatingText(PoseStack, MultiBufferSource, String, double, double, double, int, float, boolean, float, boolean)}
	 */
	//#if 11600 <= MC && MC < 11700
	//$$ @SuppressWarnings("deprecation")
	//#endif
	public static void drawString(PoseStack matrixStack, BlockPos pos, float tickDelta, float line, String[] texts, int[] colors)
	{
		Minecraft client = Minecraft.getInstance();
		Camera camera = client.gameRenderer.getMainCamera();
		if (camera.isInitialized() && client.getEntityRenderDispatcher().options != null && client.player != null)
		{
			double x = (double)pos.getX() + 0.5D;
			double y = (double)pos.getY() + 0.5D;
			double z = (double)pos.getZ() + 0.5D;
			if (client.player.distanceToSqr(x, y, z) > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE)
			{
				return;
			}
			double camX = camera.getPosition().x;
			double camY = camera.getPosition().y;
			double camZ = camera.getPosition().z;

			// ========================== Prepare Matrix start ==========================

			//#if MC >= 11700
			matrixStack.pushPose();
			matrixStack.translate((float)(x - camX), (float)(y - camY), (float)(z - camZ));

			matrixStack.mulPoseMatrix(
					//#if MC >= 11904
					new Matrix4f().rotation(
					//#else
					//$$ new Matrix4f(
					//#endif
							camera.rotation()
					)
			);
			matrixStack.scale(
					//#if MC >= 12100
					//$$ FONT_SIZE,
					//#else
					-FONT_SIZE,
					//#endif
					-FONT_SIZE,
					1
			);
			//#if MC < 11904
			//$$ RenderSystem.enableTexture();
			//#endif

			// visibleThroughObjects
			//#if MC >= 12105
			//$$ GlStateManager._disableDepthTest();
			//#else
			RenderSystem.disableDepthTest();
			//#endif

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
			//$$ RenderSystem.multMatrix(new Matrix4f(camera.rotation()));
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
			//$$ EntityRenderDispatcher entityRenderDispatcher = client.getEntityRenderDispatcher();
			//$$ GlStateManager.rotatef(-entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
			//$$ GlStateManager.rotatef(-entityRenderDispatcher.playerRotX, 1.0F, 0.0F, 0.0F);
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
				totalWidth += client.font.width(text);
			}

			float writtenWidth = 0.0F;
			for (int i = 0; i < texts.length; i++)
			{
				float renderX = -totalWidth * 0.5F + writtenWidth;
				float renderY = client.font.wordWrapHeight(
						//#if MC >= 1.21.11
						//$$ FormattedText.of(texts[i]),
						//#else
						texts[i],
						//#endif
						Integer.MAX_VALUE
				) * (-0.5F + 1.25F * line);

				//#if MC >= 11500
				//#if MC >= 11904
				Matrix4f positionMatrix = matrixStack.last().pose();
				//#else
				//$$ Matrix4f positionMatrix = Transformation.identity().getMatrix();
				//#endif

				//#endif  // if MC >= 11500

				//#if MC >= 11500
				MultiBufferSource.BufferSource immediate = getVertexConsumer();
				client.font.drawInBatch(
						texts[i], renderX, renderY, colors[i] | (0xFF << 24),
						false, positionMatrix, immediate,
						//#if MC >= 11904
						Font.DisplayMode.SEE_THROUGH,
						//#else
						//$$ true,
						//#endif
						0, 0xF000F0
				);
				immediate.endBatch();
				//#else
				//$$ client.font.draw(texts[i], renderX, renderY, colors[i]);
				//#endif

				writtenWidth += client.font.width(texts[i]);
			}

			// ========================== Restore Matrix start ==========================

			//#if MC >= 11700

			//#if MC < 11904
			//$$ RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			//#endif

			//#if MC >= 12105
			//$$ GlStateManager._enableDepthTest();
			//#else
			RenderSystem.enableDepthTest();
			//#endif

			matrixStack.popPose();

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
