package me.fallenbreath.pistorder.mixins;

import net.minecraft.block.BlockPistonBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockPistonBase.class)
public interface PistonBlockAccessor
{
	@Accessor
	boolean getIsSticky();
}
