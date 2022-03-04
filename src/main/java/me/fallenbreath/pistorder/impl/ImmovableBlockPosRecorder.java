package me.fallenbreath.pistorder.impl;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface ImmovableBlockPosRecorder
{
	@Nullable BlockPos getImmovableBlockPos();
}
