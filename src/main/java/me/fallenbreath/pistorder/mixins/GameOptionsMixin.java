package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.utils.PistorderConfigure;
import me.fallenbreath.pistorder.utils.PistorderKeyBinding;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin
{
	@Mutable
	@Shadow @Final public KeyBinding[] keysAll;

	@Inject(method = "load", at = @At("HEAD"))
	public void loadPistorderConfigure(CallbackInfo info)
	{
		PistorderConfigure.load();
		this.keysAll = PistorderKeyBinding.updateVanillaKeyBinding(this.keysAll);
	}

	@Inject(method = "write", at = @At("HEAD"))
	public void savePistorderConfigure(CallbackInfo info)
	{
		PistorderConfigure.save();
	}
}
