package me.fallenbreath.pistorder.impl;

import com.google.common.collect.Lists;
import me.fallenbreath.pistorder.mixins.PistonBlockAccessor;
import me.fallenbreath.pistorder.pushlimit.PushLimitManager;
import me.fallenbreath.pistorder.utils.PistorderConfigure;
import me.fallenbreath.pistorder.utils.TemporaryBlockRemover;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
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
	private BlockPos immovableBlockPos;

	public final World world;
	public final BlockPos pistonPos;
	public final IBlockState blockState;
	public final EnumFacing direction;
	public PistonActionType actionType;
	public final int color;

	private DisplayMode displayMode;

	// used for dynamically_information_update
	private long lastUpdateTime = -1;

	public PistorderDisplay(World world, BlockPos pistonPos, IBlockState blockState, EnumFacing direction, PistonActionType actionType)
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
	private void analyze(World world, BlockPos simulatedPistonPos, EnumFacing pistonFacing, PistonActionType PistonActionType)
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

		BlockPistonStructureHelper pistonHandler = new BlockPistonStructureHelper(world, simulatedPistonPos, pistonFacing, PistonActionType.isPush());
		this.moveSuccess = pistonHandler.canMove();

		if (!this.moveSuccess)
		{
			int newPushLimit = Math.max(PushLimitManager.getInstance().getPushLimit(), MAX_PUSH_LIMIT_FOR_CALC);
			PushLimitManager.getInstance().overwritePushLimit(newPushLimit);
			pistonHandler.canMove();
		}

		// restoring things
		blockReplacer.restoreBlocks();
		PushLimitManager.getInstance().restorePushLimit();  // it's ok if the push limit hasn't been overwritten

		this.brokenBlocks = Lists.newArrayList(pistonHandler.getBlocksToDestroy());
		this.movedBlocks = Lists.newArrayList(pistonHandler.getBlocksToMove());
		this.immovableBlockPos = ((ImmovableBlockPosRecorder)pistonHandler).getImmovableBlockPos();
		// reverse the list for the correct order
		Collections.reverse(this.brokenBlocks);
		Collections.reverse(this.movedBlocks);
	}

	private boolean tryIndirectMode()
	{
		IBlockState blockInFront1 = this.world.getBlockState(this.pistonPos.offset(this.getPistonFacing(), 1));
		IBlockState blockInFront2 = this.world.getBlockState(this.pistonPos.offset(this.getPistonFacing(), 2));
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
		Chunk chunk = world.getChunk(this.pistonPos.getX() >> 4, this.pistonPos.getZ() >> 4);
		if (!chunk.isEmpty())  // it's really a loaded chunk
		{
			return chunk.getBlockState(this.pistonPos).equals(this.blockState);
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

			if (PistorderConfigure.DYNAMICALLY_INFORMATION_UPDATE)
			{
				if (this.world.getGameTime() != this.lastUpdateTime)
				{
					this.refreshInformation();
					this.lastUpdateTime = this.world.getGameTime();
				}
			}

			String actionKey = this.actionType.isPush() ? "pistorder.push" : "pistorder.retract";
			String actionResult = this.moveSuccess ? INDICATOR_SUCCESS : INDICATOR_FAIL;
			int goldValue = TextFormatting.GOLD.getColor();

			drawString(this.pistonPos, tickDelta, -0.5F,String.format("%s %s", I18n.format(actionKey), actionResult),goldValue);

			drawString(
					this.pistonPos, tickDelta, 0.5F,
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

			if (this.immovableBlockPos != null)
			{
				drawString(this.immovableBlockPos, tickDelta, 0.0F, "×", Formatting.DARK_RED.getColorValue());
			}
		}
	}
}
