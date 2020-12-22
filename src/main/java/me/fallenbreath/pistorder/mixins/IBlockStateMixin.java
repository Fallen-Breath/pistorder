package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.Pistorder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IBlockState.class)
public abstract class IBlockStateMixin
{
	@Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
	private void onPlayerRightClickBlock(World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir)
	{
		if (world.isRemote)
		{
			EnumActionResult result = Pistorder.getInstance().onPlayerRightClickBlock(world, player, hand, pos);
			if (result == EnumActionResult.SUCCESS)
			{
				cir.setReturnValue(true);
			}
		}
	}
}
