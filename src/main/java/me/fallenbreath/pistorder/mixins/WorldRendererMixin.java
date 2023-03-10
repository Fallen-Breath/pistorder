package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.impl.Pistorder;
import me.fallenbreath.pistorder.utils.RenderContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin
{
	@ModifyVariable(
			method = "render",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/MinecraftClient;debugRenderer:Lnet/minecraft/client/render/debug/DebugRenderer;"
			)
	)
	private VertexConsumerProvider.Immediate storeVertexConsumers$pistorder(VertexConsumerProvider.Immediate vertexConsumers)
	{
		RenderContext.vertexConsumers = vertexConsumers;
		return vertexConsumers;
	}

	/**
	 * The way this.client.debugRenderer gets rendered in {@link WorldRenderer#render}
	 */
	@Inject(
			method = "render",
			slice = @Slice(
					from = @At(
							value = "FIELD",
							target = "Lnet/minecraft/client/MinecraftClient;debugRenderer:Lnet/minecraft/client/render/debug/DebugRenderer;"
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;drawCurrentLayer()V",
					ordinal = 0
			)
	)
	private void renderPistorder(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci)
	{
		RenderContext.matrices = matrices;
		Pistorder.getInstance().render(tickDelta);
	}
}
