package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.impl.ImmovableBlockPosRecorder;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(BlockPistonStructureHelper.class)
public abstract class BlockPistonStructureHelperMixin implements ImmovableBlockPosRecorder
{
	@Shadow @Final private BlockPos blockToMove;

	@Unique
	private BlockPos immovableBlockPos = null;

	@Inject(method = "canMove", at = @At("HEAD"))
	private void resetImmovableBlockPos(CallbackInfoReturnable<Boolean> cir)
	{
		this.immovableBlockPos = null;
	}

	@Inject(
			method = "canMove",
			at = @At(
					value = "RETURN",
					ordinal = 1
			)
	)
	private void recordImmovableBlockPos(CallbackInfoReturnable<Boolean> cir)
	{
		this.immovableBlockPos = this.blockToMove;
	}

	@ModifyVariable(
			method = "addBlockLine",
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/block/BlockPistonBase;canPush(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;ZLnet/minecraft/util/EnumFacing;)Z",
							ordinal = 2
					)
			),
			at = @At(
					value = "RETURN",
					ordinal = 0
			),
			ordinal = 1
	)
	private BlockPos recordImmovableBlockPos(BlockPos blockPos)
	{
		this.immovableBlockPos = blockPos;
		return blockPos;
	}

	@Override
	public @Nullable BlockPos getImmovableBlockPos()
	{
		return this.immovableBlockPos;
	}
}
