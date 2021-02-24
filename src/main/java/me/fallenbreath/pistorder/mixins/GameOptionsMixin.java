package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.utils.PistorderConfigure;
import me.fallenbreath.pistorder.utils.PistorderKeyBinding;
import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameSettings.class)
public abstract class GameOptionsMixin
{
	@Mutable
	@Shadow public KeyBinding[] keyBindings;

	@Inject(method = "loadOptions", at = @At("HEAD"))
	public void loadPistorderConfigure(CallbackInfo info)
	{
		PistorderConfigure.load();
		PistorderConfigure.save();
		this.keyBindings = PistorderKeyBinding.updateVanillaKeyBinding(this.keyBindings);
	}

	@Inject(method = "saveOptions", at = @At("HEAD"))
	public void savePistorderConfigure(CallbackInfo info)
	{
		PistorderConfigure.save();
	}
}
