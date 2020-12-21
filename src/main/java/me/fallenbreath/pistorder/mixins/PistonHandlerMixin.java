package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.pushlimit.PushLimitManager;
import net.minecraft.block.piston.PistonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PistonHandler.class)
public abstract class PistonHandlerMixin
{
	@ModifyConstant(method = "tryMove", constant = @Constant(intValue = 12), require = 3, allow = 3)
	private int modifyPushLimitPistorderMod(int value)
	{
		return PushLimitManager.getInstance().getPushLimit();
	}
}
