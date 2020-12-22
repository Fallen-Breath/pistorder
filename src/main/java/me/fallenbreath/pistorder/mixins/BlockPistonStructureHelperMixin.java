package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.pushlimit.PushLimitManager;
import net.minecraft.block.state.BlockPistonStructureHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockPistonStructureHelper.class)
public abstract class BlockPistonStructureHelperMixin
{
	@ModifyConstant(method = "addBlockLine", constant = @Constant(intValue = 12), require = 3, allow = 3)
	private int modifyPushLimitPistorderMod(int value)
	{
		return PushLimitManager.getInstance().getPushLimit();
	}
}
