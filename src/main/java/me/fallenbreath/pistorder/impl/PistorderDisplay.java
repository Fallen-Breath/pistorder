package me.fallenbreath.pistorder.impl;

import me.fallenbreath.pistorder.mixins.PistonBlockAccessor;
import me.fallenbreath.pistorder.pushlimit.PushLimitManager;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PistorderDisplay
{
	private static final double MAX_RENDER_DISTANCE = 256.0D;
	private static final int MAX_PUSH_LIMIT_FOR_CALC = 128;
	private static final float FONT_SIZE = 0.025F;

	private static final String INDICATOR_SUCCESS = TextFormatting.GREEN + "√";
	private static final String INDICATOR_FAIL = TextFormatting.RED + "×";

	private List<BlockPos> movedBlocks;
	private List<BlockPos> brokenBlocks;
	private boolean moveSuccess;

	public final World world;
	public final BlockPos pos;
	public final IBlockState blockState;
	public final EnumFacing direction;
	public PistonActionType actionType;
	public final int color;

	private DisplayMode displayMode;

	public PistorderDisplay(World world, BlockPos pos, IBlockState blockState, EnumFacing direction, PistonActionType actionType)
	{
		this.world = world;
		this.pos = pos;
		this.blockState = blockState;
		this.direction = direction;
		this.actionType = actionType;
		this.setDisplayMode(DisplayMode.DIRECT);
		Random random = new Random(pos.hashCode());
		this.color = (random.nextInt(0x50) + 0xAF) + ((random.nextInt(0x50) + 0xAF) << 8) + ((random.nextInt(0x50) + 0xAF) << 16);
	}

	private EnumFacing getPistonFacing()
	{
		return this.blockState.get(BlockStateProperties.FACING);
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

	private void setDisplayMode(DisplayMode mode)
	{
		this.displayMode = mode;
		BlockPos startPos = null;
		switch (this.displayMode)
		{
			case DIRECT:
				startPos = this.pos;
				break;
			case INDIRECT:
				startPos = this.pos;
				if (!this.isStickyPiston())
				{
					startPos = startPos.offset(this.getPistonFacing());
				}
				break;
			case DISABLED:
				break;
		}
		if (startPos != null)
		{
			this.analyze(this.world, startPos, this.getPistonFacing(), this.actionType);
		}
	}

	private void analyze(World world, BlockPos pos, EnumFacing pistonFacing, PistonActionType PistonActionType)
	{
		IBlockState[] states = new IBlockState[2];
		if (PistonActionType.isRetract())
		{
			states[0] = world.getBlockState(pos);  // piston base
			states[1] = world.getBlockState(pos.offset(pistonFacing));  // piston head
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
			world.setBlockState(pos.offset(pistonFacing), Blocks.AIR.getDefaultState(), 18);
		}

		BlockPistonStructureHelper pistonHandler = new BlockPistonStructureHelper(world, pos, pistonFacing, PistonActionType.isPush());
		this.moveSuccess = pistonHandler.canMove();

		if (!this.moveSuccess)
		{
			PushLimitManager.getInstance().overwritePushLimit(MAX_PUSH_LIMIT_FOR_CALC);
			pistonHandler.canMove();
		}

		this.brokenBlocks = pistonHandler.getBlocksToDestroy();
		this.movedBlocks = pistonHandler.getBlocksToMove();
		// reverse the list for correct order
		Collections.reverse(this.brokenBlocks);
		Collections.reverse(this.movedBlocks);

		PushLimitManager.getInstance().restorePushLimit();

		if (PistonActionType.isRetract())
		{
			world.setBlockState(pos, states[0], 18);
			world.setBlockState(pos.offset(pistonFacing), states[1], 18);
		}
	}

	private boolean tryIndirectMode()
	{
		IBlockState blockInFront1 = this.world.getBlockState(this.pos.offset(this.getPistonFacing(), 1));
		IBlockState blockInFront2 = this.world.getBlockState(this.pos.offset(this.getPistonFacing(), 2));
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

	/**
	 * Stolen from {@link DebugRenderer#renderDebugText(String, double, double, double, float, int)}
	 */
	private static void drawString(BlockPos pos, float tickDelta, float line, String[] texts, int[] colors)
	{
		Minecraft client = Minecraft.getInstance();
//		Camera camera = client.gameRenderer.getCamera();
		if (client.getRenderManager().options != null && client.player != null)
		{
			double x = (double)pos.getX() + 0.5D;
			double y = (double)pos.getY() + 0.5D;
			double z = (double)pos.getZ() + 0.5D;
			if (client.player.getDistanceSq(x, y, z) > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE)
			{
				return;
			}

			EntityPlayer entityplayer = client.player;
			double camX = entityplayer.lastTickPosX + (entityplayer.posX - entityplayer.lastTickPosX) * (double)tickDelta;
			double camY = entityplayer.lastTickPosY + (entityplayer.posY - entityplayer.lastTickPosY) * (double)tickDelta;
			double camZ = entityplayer.lastTickPosZ + (entityplayer.posZ - entityplayer.lastTickPosZ) * (double)tickDelta;

			GlStateManager.pushMatrix();
			GlStateManager.translatef((float)(x - camX), (float)(y - camY), (float)(z - camZ));
			GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
			GlStateManager.scalef(FONT_SIZE, -FONT_SIZE, FONT_SIZE);
			RenderManager rendermanager = client.getRenderManager();
			GlStateManager.rotatef(-rendermanager.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef((float)(rendermanager.options.thirdPersonView == 2 ? 1 : -1) * rendermanager.playerViewX, 1.0F, 0.0F, 0.0F);
			GlStateManager.disableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.disableDepthTest();  // visibleThroughObjects
			GlStateManager.depthMask(true);
			GlStateManager.scalef(-1.0F, 1.0F, 1.0F);
			float totalWidth = 0.0F;
			for (String text: texts)
			{
				totalWidth += client.fontRenderer.getStringWidth(text);
			}

			float writtenWidth = 0.0F;
			for (int i = 0; i < texts.length; i++)
			{
				float renderX = -totalWidth * 0.5F + writtenWidth;
				float renderY = client.fontRenderer.getWordWrappedHeight(texts[i], Integer.MAX_VALUE) * (-0.5F + 1.25F * line);
				client.fontRenderer.drawString(texts[i], renderX, renderY, colors[i]);

				writtenWidth += client.fontRenderer.getStringWidth(texts[i]);
			}

			GlStateManager.enableLighting();
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.popMatrix();
		}
	}

	private static void drawString(BlockPos pos, float tickDelta, float line, String text, int color)
	{
		drawString(pos, tickDelta, line, new String[]{text}, new int[]{color});
	}

	private boolean checkState(World world)
	{
		if (!Objects.equals(world, this.world))
		{
			return false;
		}
		Chunk chunk = world.getChunk(this.pos.getX() >> 4, this.pos.getZ() >> 4);
		if (chunk.isEmpty())  // it's a real loaded chunk
		{
			return chunk.getBlockState(this.pos).equals(this.blockState);
		}
		return true;
	}

	@SuppressWarnings("ConstantConditions")
	void render(float tickDelta)
	{
		if (!this.isDisabled())
		{
			Minecraft client = Minecraft.getInstance();
			if (!this.checkState(client.world))
			{
				this.disable();
				return;
			}

			String actionKey = this.actionType.isPush() ? "pistorder.push" : "pistorder.retract";
			String actionResult = this.moveSuccess ? INDICATOR_SUCCESS : INDICATOR_FAIL;
			int goldValue = TextFormatting.GOLD.getColor();

			drawString(this.pos, tickDelta, -0.5F,String.format("%s %s", I18n.format(actionKey), actionResult),goldValue);

			drawString(
					this.pos, tickDelta, 0.5F,
					new String[]{
							I18n.format("pistorder.block_count.pre"),
							String.valueOf(this.movedBlocks.size()),
							I18n.format("pistorder.block_count.post")
					},
					new int[]{goldValue, this.color, goldValue}
			);

			for (int i = 0; i < this.movedBlocks.size(); i++)
			{
				drawString(this.movedBlocks.get(i), tickDelta, 0.0F, String.valueOf(i + 1), this.color);
			}
			for (int i = 0; i < this.brokenBlocks.size(); i++)
			{
				drawString(this.brokenBlocks.get(i), tickDelta, 0.0F, String.valueOf(i + 1), TextFormatting.RED.getColor());
			}
		}
	}
}
