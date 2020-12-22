package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.Pistorder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockPistonBase.class)
public abstract class BlockPistonBaseMixin extends Block
{
	public BlockPistonBaseMixin(Properties properties)
	{
		super(properties);
	}

	@SuppressWarnings("deprecation")
	@Intrinsic
	@Override
	public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (worldIn.isRemote)
		{
			EnumActionResult result = Pistorder.getInstance().onPlayerRightClickBlock(worldIn, player, hand, pos);
			return result == EnumActionResult.SUCCESS;
		}
		return false;
	}
}
