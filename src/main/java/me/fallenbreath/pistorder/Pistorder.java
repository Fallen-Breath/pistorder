package me.fallenbreath.pistorder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import me.fallenbreath.pistorder.mixins.PistonBlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

public class Pistorder
{
	private static final Pistorder INSTANCE = new Pistorder();

	private final Map<Pair<World, BlockPos>, PistorderDisplay> displayMap = Maps.newHashMap();

	public static Pistorder getInstance()
	{
		return INSTANCE;
	}

	public EnumActionResult onPlayerRightClickBlock(World world, EntityPlayer player, EnumHand hand, BlockPos pos)
	{
		// click with empty main hand, not sneaking
		if (hand == EnumHand.MAIN_HAND && player.getHeldItemMainhand().isEmpty() && !player.isSneaking())
		{
			IBlockState blockState = world.getBlockState(pos);
			Block block = blockState.getBlock();
			if (block instanceof BlockPistonBase)
			{
				boolean extended = blockState.get(BlockPistonBase.EXTENDED);
				if (!extended || ((PistonBlockAccessor)block).getIsSticky())
				{
					this.click(world, pos, blockState, blockState.get(BlockStateProperties.FACING), extended ? PistonActionType.RETRACT : PistonActionType.PUSH);
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.FAIL;
	}

	synchronized private void click(World world, BlockPos pos, IBlockState blockState, EnumFacing pistonFacing, PistonActionType actionType)
	{
		Pair<World, BlockPos> key = Pair.of(world, pos);
		PistorderDisplay display = this.displayMap.get(key);
		if (display == null)
		{
			this.displayMap.put(key, new PistorderDisplay(world, pos, blockState, pistonFacing, actionType));
		}
		else
		{
			display.onClick();
			if (display.isDisabled())
			{
				this.displayMap.remove(key);
			}
		}
	}

	public void render(float tickDelta)
	{
		List<Pair<World, BlockPos>> removeList = Lists.newArrayList();
		this.displayMap.forEach((key, display) -> {
			display.render(tickDelta);
			if (display.isDisabled())
			{
				removeList.add(key);
			}
		});
		removeList.forEach(this.displayMap::remove);
	}
}
