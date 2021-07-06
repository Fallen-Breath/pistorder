package me.fallenbreath.pistorder.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import me.fallenbreath.pistorder.impl.Pistorder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
	@Inject(method = "render", at = @At(value = "RETURN"))
	private void renderPistorder(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		matrixStack.method_34425(matrices.peek().getModel());
		RenderSystem.applyModelViewMatrix();
		Pistorder.getInstance().render(tickDelta);
		matrixStack.pop();
		RenderSystem.applyModelViewMatrix();
	}
}
