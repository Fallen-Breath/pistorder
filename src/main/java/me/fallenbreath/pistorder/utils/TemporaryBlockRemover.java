package me.fallenbreath.pistorder.utils;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class TemporaryBlockRemover
{
	private static final int SET_BLOCK_FLAGS = 16;  // no block/state/listener update

	private final World world;
	private final List<BlockPos> blockPosList = Lists.newArrayList();
	private final List<BlockState> blockStateList = Lists.newArrayList();

	public TemporaryBlockRemover(World world)
	{
		this.world = world;
	}

	public void add(BlockPos pos)
	{
		this.blockPosList.add(pos);
	}

	public void removeBlocks()
	{
		this.blockStateList.clear();
		this.blockPosList.stream().map(this.world::getBlockState).forEach(this.blockStateList::add);
		this.blockPosList.forEach(pos -> this.world.setBlockState(pos, Blocks.AIR.getDefaultState(), SET_BLOCK_FLAGS));
	}

	public void restoreBlocks()
	{
		for (int i = 0; i < this.blockPosList.size(); i++)
		{
			this.world.setBlockState(this.blockPosList.get(i), this.blockStateList.get(i), SET_BLOCK_FLAGS);
		}
	}
}
