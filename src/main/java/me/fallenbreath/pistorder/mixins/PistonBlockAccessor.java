package me.fallenbreath.pistorder.mixins;

import net.minecraft.block.PistonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PistonBlock.class)
public interface PistonBlockAccessor
{
	@Accessor("sticky")
	boolean getIsSticky();
}
