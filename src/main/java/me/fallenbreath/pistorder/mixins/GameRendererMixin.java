package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.Pistorder;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
{
	// just like onRenderWorldLast in malilib
	@Inject(
			method = "renderCenter",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z"
			)
	)
	private void renderPistorder(float tickDelta, long endTime, CallbackInfo ci)
	{
		Pistorder.getInstance().render(tickDelta);
	}
}
