package me.fallenbreath.pistorder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import me.fallenbreath.pistorder.mixins.PistonBlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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

	public ActionResult onPlayerRightClickBlock(World world, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		// click with empty main hand, not sneaking
		if (hand == Hand.MAIN_HAND && player.getMainHandStack().isEmpty() && !player.isSneaking())
		{
			BlockPos pos = hit.getBlockPos();
			BlockState blockState = world.getBlockState(pos);
			Block block = blockState.getBlock();
			if (block instanceof PistonBlock)
			{
				boolean extended = blockState.get(PistonBlock.EXTENDED);
				if (!extended || ((PistonBlockAccessor)block).getIsSticky())
				{
					this.click(world, pos, blockState, blockState.get(Properties.FACING), extended ? PistonActionType.RETRACT : PistonActionType.PUSH);
					return ActionResult.SUCCESS;
				}
			}
		}
		return ActionResult.FAIL;
	}

	synchronized private void click(World world, BlockPos pos, BlockState blockState, Direction pistonFacing, PistonActionType PistonActionType)
	{
		Pair<World, BlockPos> key = Pair.of(world, pos);
		PistorderDisplay display = this.displayMap.get(key);
		if (display == null)
		{
			this.displayMap.put(key, new PistorderDisplay(world, pos, blockState, pistonFacing, PistonActionType));
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
