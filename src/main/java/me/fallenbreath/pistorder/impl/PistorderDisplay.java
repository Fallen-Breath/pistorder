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
import me.fallenbreath.pistorder.mixins.PistonBlockAccessor;
import me.fallenbreath.pistorder.pushlimit.PushLimitManager;
import me.fallenbreath.pistorder.utils.PistorderConfigure;
import me.fallenbreath.pistorder.utils.TemporaryBlockReplacer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PistorderDisplay
{
	private static final int MAX_PUSH_LIMIT_FOR_CALC = 128;

	private static final String INDICATOR_SUCCESS = Formatting.GREEN + "√";
	private static final String INDICATOR_FAIL = Formatting.RED + "×";

	private List<BlockPos> movedBlocks;
	private List<BlockPos> brokenBlocks;
	private boolean moveSuccess;
	private BlockPos immovableBlockPos;

	public final World world;
	public final BlockPos pistonPos;
	public final BlockState blockState;
	public final Direction direction;
	public PistonActionType actionType;
	public final int color;

	private DisplayMode displayMode;

	// used for dynamically_information_update
	private long lastUpdateTime = -1;

	public PistorderDisplay(World world, BlockPos pistonPos, BlockState blockState, Direction direction, PistonActionType actionType)
	{
		this.world = world;
		this.pistonPos = pistonPos;
		this.blockState = blockState;
		this.direction = direction;
		this.actionType = actionType;
		this.setDisplayMode(DisplayMode.DIRECT);
		Random random = new Random(pistonPos.hashCode());
		this.color = (random.nextInt(0x50) + 0xAF) + ((random.nextInt(0x50) + 0xAF) << 8) + ((random.nextInt(0x50) + 0xAF) << 16);
	}

	private Direction getPistonFacing()
	{
		return this.blockState.get(Properties.FACING);
	}

	private boolean isStickyPiston()
	{
		return ((PistonBlockAccessor)this.blockState.getBlock()).getIsSticky();
	}

	public boolean isDisabled()
	{
		return this.displayMode == DisplayMode.DISABLED;
	}

	private void disable()
	{
		this.displayMode = DisplayMode.DISABLED;
	}

	/**
	 * Will trigger a refresh
	 */
	private void setDisplayMode(DisplayMode mode)
	{
		this.displayMode = mode;
		this.refreshInformation();
	}

	private void refreshInformation()
	{
		BlockPos simulatedPistonPos = null;
		switch (this.displayMode)
		{
			case DIRECT:
				simulatedPistonPos = this.pistonPos;
				break;
			case INDIRECT:
				simulatedPistonPos = this.pistonPos;
				if (!this.isStickyPiston())
				{
					simulatedPistonPos = simulatedPistonPos.offset(this.getPistonFacing());
				}
				break;
			case DISABLED:
				break;
		}
		if (simulatedPistonPos != null)
		{
			this.analyze(this.world, simulatedPistonPos, this.getPistonFacing(), this.actionType);
		}
	}

	/**
	 * Might make the piston blink for a while if the action type is retract
	 */
	private void analyze(World world, BlockPos simulatedPistonPos, Direction pistonFacing, PistonActionType PistonActionType)
	{
		BlockState air = Blocks.AIR.getDefaultState();

		// backing up block states for potential piston (head) block position
		TemporaryBlockReplacer blockReplacer = new TemporaryBlockReplacer(this.world);
		// not necessary to replace the piston pos with barrier or something in-movable since vanilla PistonHandler handles it
		if (!this.pistonPos.equals(simulatedPistonPos))
		{
			blockReplacer.add(this.pistonPos, air);  // piston pos, in case it's in-direct mode
		}
		if (PistonActionType.isRetract())
		{
			blockReplacer.add(simulatedPistonPos.offset(pistonFacing), air);  // simulated piston head
		}
		blockReplacer.removeBlocks();

		PistonHandler pistonHandler = new PistonHandler(world, simulatedPistonPos, pistonFacing, PistonActionType.isPush());
		this.moveSuccess = pistonHandler.calculatePush();

		if (!this.moveSuccess)
		{
			int newPushLimit = Math.max(PushLimitManager.getInstance().getPushLimit(), MAX_PUSH_LIMIT_FOR_CALC);
			PushLimitManager.getInstance().overwritePushLimit(newPushLimit);
			pistonHandler.calculatePush();
		}

		// restoring things
		blockReplacer.restoreBlocks();
		PushLimitManager.getInstance().restorePushLimit();  // it's ok if the push limit hasn't been overwritten

		this.brokenBlocks = Lists.newArrayList(pistonHandler.getBrokenBlocks());
		this.movedBlocks = Lists.newArrayList(pistonHandler.getMovedBlocks());
		this.immovableBlockPos = ((ImmovableBlockPosRecorder)pistonHandler).getImmovableBlockPos();
		// reverse the list for the correct order
		Collections.reverse(this.brokenBlocks);
		Collections.reverse(this.movedBlocks);
	}

	private boolean tryIndirectMode()
	{
		BlockState blockInFront1 = this.world.getBlockState(this.pistonPos.offset(this.getPistonFacing(), 1));
		BlockState blockInFront2 = this.world.getBlockState(this.pistonPos.offset(this.getPistonFacing(), 2));
		if (blockInFront1.isAir() && !blockInFront2.isAir())
		{
			if (this.isStickyPiston())
			{
				this.actionType = PistonActionType.RETRACT;
			}
			this.setDisplayMode(DisplayMode.INDIRECT);
			return true;
		}
		return false;
	}

	void onClick()
	{
		switch (this.displayMode)
		{
			case DIRECT:
				if (!this.tryIndirectMode())
				{
					this.disable();
				}
				break;
			case INDIRECT:
				this.disable();
				break;
			case DISABLED:
				// do nothing
				break;
		}
	}

	private static void drawString(MatrixStack matrixStack, BlockPos pos, float tickDelta, float line, String text, int color)
	{
		StringDrawer.drawString(matrixStack, pos, tickDelta, line, new String[]{text}, new int[]{color});
	}

	private boolean checkState(World world)
	{
		if (!Objects.equals(world, this.world))
		{
			return false;
		}
		BlockView chunk = world.getChunkManager().getChunk(this.pistonPos.getX() >> 4, this.pistonPos.getZ() >> 4);
		if (chunk instanceof WorldChunk && !((WorldChunk)chunk).isEmpty())  // it's really a loaded chunk
		{
			return chunk.getBlockState(this.pistonPos).equals(this.blockState);
		}
		return true;
	}

	@SuppressWarnings("ConstantConditions")
	void render(MatrixStack matrixStack, float tickDelta)
	{
		if (!this.isDisabled())
		{
			MinecraftClient client = MinecraftClient.getInstance();
			if (!this.checkState(client.world))
			{
				this.disable();
				return;
			}

			if (PistorderConfigure.DYNAMICALLY_INFORMATION_UPDATE)
			{
				if (this.world.getTime() != this.lastUpdateTime)
				{
					this.refreshInformation();
					this.lastUpdateTime = this.world.getTime();
				}
			}

			String actionKey = this.actionType.isPush() ? "pistorder.push" : "pistorder.retract";
			String actionResult = this.moveSuccess ? INDICATOR_SUCCESS : INDICATOR_FAIL;
			int goldValue = Formatting.GOLD.getColorValue();

			drawString(matrixStack, this.pistonPos, tickDelta, -0.5F, String.format("%s %s", I18n.translate(actionKey), actionResult), goldValue);

			StringDrawer.drawString(
					matrixStack, this.pistonPos, tickDelta, 0.5F,
					new String[]{
							I18n.translate("pistorder.block_count.pre"),
							String.valueOf(this.movedBlocks.size()),
							I18n.translate("pistorder.block_count.post")
					},
					new int[]{goldValue, this.color, goldValue}
			);

			for (int i = 0; i < this.movedBlocks.size(); i++)
			{
				drawString(matrixStack, this.movedBlocks.get(i), tickDelta, 0.0F, String.valueOf(i + 1), this.color);
			}
			for (int i = 0; i < this.brokenBlocks.size(); i++)
			{
				drawString(matrixStack, this.brokenBlocks.get(i), tickDelta, 0.0F, String.valueOf(i + 1), Formatting.RED.getColorValue());
			}

			if (this.immovableBlockPos != null)
			{
				drawString(matrixStack, this.immovableBlockPos, tickDelta, 0.0F, "×", Formatting.DARK_RED.getColorValue());
			}
		}
	}
}
