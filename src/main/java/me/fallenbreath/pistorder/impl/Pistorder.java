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

package me.fallenbreath.pistorder.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import me.fallenbreath.pistorder.mixins.PistonBlockAccessor;
import me.fallenbreath.pistorder.utils.PistorderKeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.Map;

//#if MC >= 11900
import net.minecraft.network.chat.Component;
//#else
//$$ import net.minecraft.network.chat.TranslatableComponent;
//#endif

public class Pistorder
{
	private static final Pistorder INSTANCE = new Pistorder();

	private final Map<Pair<Level, BlockPos>, PistorderDisplay> displayMap = Maps.newHashMap();

	public static Pistorder getInstance()
	{
		return INSTANCE;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isEnabled()
	{
		return !TweakerMoreCompact.isTweakerMoreVersionEnabled();
	}

	public InteractionResult onPlayerRightClickBlock(Level world, Player player, InteractionHand hand, BlockHitResult hit)
	{
		if (!this.isEnabled())
		{
			return InteractionResult.FAIL;
		}

		// click with empty main hand, not sneaking
		if (hand == InteractionHand.MAIN_HAND)
		{
			return this.onPlayerRightClickBlockWithMainHand(world, player, hit);
		}
		return InteractionResult.FAIL;
	}

	public InteractionResult onPlayerRightClickBlockWithMainHand(Level world, Player player, BlockHitResult hit)
	{
		if (!this.isEnabled())
		{
			return InteractionResult.FAIL;
		}

		if (player.getMainHandItem().isEmpty() && !player.isShiftKeyDown())
		{
			BlockPos pos = hit.getBlockPos();
			BlockState blockState = world.getBlockState(pos);
			Block block = blockState.getBlock();
			if (block instanceof PistonBaseBlock)
			{
				boolean extended = blockState.getValue(PistonBaseBlock.EXTENDED);
				if (!extended || ((PistonBlockAccessor)block).getIsSticky())
				{
					this.click(world, pos, blockState, blockState.getValue(BlockStateProperties.FACING), extended ? PistonActionType.RETRACT : PistonActionType.PUSH);
					return InteractionResult.SUCCESS;
				}
			}
		}

		return InteractionResult.FAIL;
	}

	synchronized private void click(Level world, BlockPos pos, BlockState blockState, Direction pistonFacing, PistonActionType PistonActionType)
	{
		Pair<Level, BlockPos> key = Pair.of(world, pos);
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

	public void render(PoseStack matrixStack, float tickDelta)
	{
		if (!this.isEnabled())
		{
			this.displayMap.clear();
			return;
		}

		this.tickKeyBinding();
		List<Pair<Level, BlockPos>> removeList = Lists.newArrayList();
		this.displayMap.forEach((key, display) -> {
			display.render(matrixStack, tickDelta);
			if (display.isDisabled())
			{
				removeList.add(key);
			}
		});
		removeList.forEach(this.displayMap::remove);
	}

	private void tickKeyBinding()
	{
		if (PistorderKeyBinding.CLEAR_DISPLAY_KEY.consumeClick())
		{
			Minecraft.getInstance().gui.setOverlayMessage(
					//#if MC >= 11904
					Component.translatable(
					//#else
					//$$ new TranslatableComponent(
					//#endif
							"pistorder.clear_display.hint"
					),
					false
			);
			this.displayMap.clear();
		}
	}
}
