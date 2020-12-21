package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.Pistorder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin
{
	@Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
	private void onPlayerRightClickBlock(World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir)
	{
		if (world.isClient)
		{
			ActionResult result = Pistorder.getInstance().onPlayerRightClickBlock(world, player, hand, hit);
			if (result.isAccepted())
			{
				cir.setReturnValue(result);
			}
		}
	}
}
