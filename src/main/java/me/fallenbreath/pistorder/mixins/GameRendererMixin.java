package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.impl.Pistorder;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
{
	// just like onRenderWorldLast in malilib
	@Inject(
			method = "updateCameraAndRender(FJ)V",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"
			)
	)
	private void renderPistorder(float partialTicks, long nanoTime, CallbackInfo ci)
	{
		Pistorder.getInstance().render(partialTicks);
	}
}
