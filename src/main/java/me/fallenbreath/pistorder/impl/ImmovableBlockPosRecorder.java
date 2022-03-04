package me.fallenbreath.pistorder.impl;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface ImmovableBlockPosRecorder
{
	@Nullable BlockPos getImmovableBlockPos();
}
