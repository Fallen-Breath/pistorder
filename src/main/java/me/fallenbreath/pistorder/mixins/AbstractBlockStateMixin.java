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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 11600
import net.minecraft.block.AbstractBlock;
//#else
//$$ import net.minecraft.block.BlockState;
//#endif

@Mixin(
		//#if MC >= 11600
		AbstractBlock.AbstractBlockState.class
		//#else
		//$$ BlockState.class
		//#endif
)
public abstract class AbstractBlockStateMixin
{
	@Inject(
			//#if MC >= 11500
			method = "onUse",
			//#else
			//$$ method = "activate",
			//#endif
			at = @At("HEAD"),
			cancellable = true
	)
	private void onPlayerRightClickBlock(
			World world, PlayerEntity player,
			//#if MC < 12005
			Hand hand,
			//#endif
			BlockHitResult hit,

			//#if MC >= 11500
			CallbackInfoReturnable<ActionResult> cir
			//#else
			//$$ CallbackInfoReturnable<Boolean> cir
			//#endif
	)
	{
		if (world.isClient)
		{
			ActionResult result = Pistorder.getInstance().
					//#if MC >= 12005
					//$$ onPlayerRightClickBlockWithMainHand(world, player, hit);
					//#else
					onPlayerRightClickBlock(world, player, hand, hit);
					//#endif

			//#if MC >= 11500
			boolean ok = result.isAccepted();
			//#else
			//$$ boolean ok = result == ActionResult.SUCCESS;
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
