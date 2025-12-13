/*
 * This file is part of the Pistorder project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
 *
 * Pistorder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pistorder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pistorder.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.impl.Pistorder;
import me.fallenbreath.pistorder.utils.PistorderConfigure;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 11600
import net.minecraft.world.level.block.state.BlockBehaviour;
//#else
//$$ import net.minecraft.world.level.block.state.BlockState;
//#endif

@Mixin(
		//#if MC >= 11600
		BlockBehaviour.BlockStateBase.class
		//#else
		//$$ BlockState.class
		//#endif
)
public abstract class AbstractBlockStateMixin
{
	@Inject(
			//#if MC >= 12005
			//$$ method = "useWithoutItem",
			//#else
			method = "use",
			//#endif
			at = @At("HEAD"),
			cancellable = true
	)
	private void onPlayerRightClickBlock(
			Level world, Player player,
			//#if MC < 12005
			InteractionHand hand,
			//#endif
			BlockHitResult hit,

			//#if MC >= 11500
			CallbackInfoReturnable<InteractionResult> cir
			//#else
			//$$ CallbackInfoReturnable<Boolean> cir
			//#endif
	)
	{
		if (world.isClientSide())
		{
			InteractionResult result = Pistorder.getInstance().
					//#if MC >= 12005
					//$$ onPlayerRightClickBlockWithMainHand(world, player, hit);
					//#else
					onPlayerRightClickBlock(world, player, hand, hit);
					//#endif

			//#if MC >= 11500
			boolean ok = result.consumesAction();
			//#else
			//$$ boolean ok = result == InteractionResult.SUCCESS;
			//#endif

			if (ok && PistorderConfigure.SWING_HAND)
			{
				cir.setReturnValue(
						//#if MC >= 11500
						result
						//#else
						//$$ true
						//#endif
				);
			}
		}
	}
}
