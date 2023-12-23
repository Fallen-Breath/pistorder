package me.fallenbreath.pistorder.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;

//#if MC < 11904
//$$ import net.minecraft.util.math.AffineTransformation;
//#endif

public class StringDrawer
{
	private static final double MAX_RENDER_DISTANCE = 256.0D;
	private static final float FONT_SIZE = 0.025F;

	/**
	 * Stolen from {@link DebugRenderer#drawString(MatrixStack, VertexConsumerProvider, String, double, double, double, int, float, boolean, float, boolean)}
	 */
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
			matrixStack.push();
			matrixStack.translate((float)(x - camX), (float)(y - camY), (float)(z - camZ));
			matrixStack.multiplyPositionMatrix(
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

				VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
				//#if MC >= 11904
				Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
				//#else
				//$$ Matrix4f positionMatrix = AffineTransformation.identity().getMatrix();
				//#endif
				client.textRenderer.draw(
						texts[i], renderX, renderY, colors[i], false, positionMatrix, immediate,
						//#if MC >= 11904
						TextRenderer.TextLayerType.SEE_THROUGH,
						//#else
						//$$ true,
						//#endif
						0, 0xF000F0
				);
				immediate.draw();

				writtenWidth += client.textRenderer.getWidth(texts[i]);
			}

			//#if MC < 11904
			//$$ RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			//#endif

			RenderSystem.enableDepthTest();
			matrixStack.pop();
		}
	}
}
