package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.Pistorder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer
{
	@Shadow @Final private MinecraftClient client;

	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE_STRING",
					args = "ldc=weather",
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V"
			)
	)
	private void onRenderWorldLast(CallbackInfo ci)
	{
		Pistorder.getInstance().render();
	}
}
