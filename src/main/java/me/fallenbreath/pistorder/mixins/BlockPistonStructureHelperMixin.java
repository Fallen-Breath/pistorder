package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.impl.ImmovableBlockPosRecorder;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonHandler.class)
public abstract class PistonHandlerMixin implements ImmovableBlockPosRecorder
{
	@Shadow @Final private BlockPos posTo;

	@Unique
	private BlockPos immovableBlockPos = null;

	@Inject(method = "calculatePush", at = @At("HEAD"))
	private void resetImmovableBlockPos(CallbackInfoReturnable<Boolean> cir)
	{
		this.immovableBlockPos = null;
	}

	@Inject(
			method = "calculatePush",
			at = @At(
					value = "RETURN",
					ordinal = 1
			)
	)
	private void recordImmovableBlockPos(CallbackInfoReturnable<Boolean> cir)
	{
		this.immovableBlockPos = this.posTo;
	}

	@ModifyVariable(
			method = "tryMove",
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/block/PistonBlock;isMovable(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;ZLnet/minecraft/util/math/Direction;)Z",
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
