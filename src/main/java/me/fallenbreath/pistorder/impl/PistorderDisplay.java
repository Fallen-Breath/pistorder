package me.fallenbreath.pistorder.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.fallenbreath.pistorder.mixins.PistonBlockAccessor;
import me.fallenbreath.pistorder.pushlimit.PushLimitManager;
import me.fallenbreath.pistorder.utils.PistorderConfigure;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.Rotation3;
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
	private static final double MAX_RENDER_DISTANCE = 256.0D;
	private static final int MAX_PUSH_LIMIT_FOR_CALC = 128;
	private static final float FONT_SIZE = 0.025F;

	private static final String INDICATOR_SUCCESS = Formatting.GREEN + "√";
	private static final String INDICATOR_FAIL = Formatting.RED + "×";

	private List<BlockPos> movedBlocks;
	private List<BlockPos> brokenBlocks;
	private boolean moveSuccess;

	public final World world;
	public final BlockPos pos;
	public final BlockState blockState;
	public final Direction direction;
	public PistonActionType actionType;
	public final int color;

	private DisplayMode displayMode;

	// used for dynamically_information_update
	private long lastUpdateTime = -1;

	public PistorderDisplay(World world, BlockPos pos, BlockState blockState, Direction direction, PistonActionType actionType)
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

	/**
	 * Might make the piston blink for a while if the action type is retract
	 */
	private void analyze(World world, BlockPos pos, Direction pistonFacing, PistonActionType PistonActionType)
	{
		BlockState[] states = new BlockState[2];
		if (PistonActionType.isRetract())
		{
			states[0] = world.getBlockState(pos);  // piston base
			states[1] = world.getBlockState(pos.offset(pistonFacing));  // piston head
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
			world.setBlockState(pos.offset(pistonFacing), Blocks.AIR.getDefaultState(), 18);
		}

		PistonHandler pistonHandler = new PistonHandler(world, pos, pistonFacing, PistonActionType.isPush());
		this.moveSuccess = pistonHandler.calculatePush();

		if (!this.moveSuccess)
		{
			PushLimitManager.getInstance().overwritePushLimit(MAX_PUSH_LIMIT_FOR_CALC);
			pistonHandler.calculatePush();
		}

		if (PistonActionType.isRetract())
		{
			world.setBlockState(pos, states[0], 18);
			world.setBlockState(pos.offset(pistonFacing), states[1], 18);
		}

		this.brokenBlocks = pistonHandler.getBrokenBlocks();
		this.movedBlocks = pistonHandler.getMovedBlocks();
		// reverse the list for correct order
		Collections.reverse(this.brokenBlocks);
		Collections.reverse(this.movedBlocks);

		PushLimitManager.getInstance().restorePushLimit();
	}

	private boolean tryIndirectMode()
	{
		BlockState blockInFront1 = this.world.getBlockState(this.pos.offset(this.getPistonFacing(), 1));
		BlockState blockInFront2 = this.world.getBlockState(this.pos.offset(this.getPistonFacing(), 2));
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
	 * Stolen from {@link DebugRenderer#drawString(String, double, double, double, int, float, boolean, float, boolean)}
	 */
	private static void drawString(BlockPos pos, float tickDelta, float line, String[] texts, int[] colors)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		Camera camera = client.gameRenderer.getCamera();
		if (camera.isReady() && client.getEntityRenderManager().gameOptions != null && client.player != null)
		{
			double x = (double)pos.getX() + 0.5D;
			double y = (double)pos.getY() + 0.5D;
			double z = (double)pos.getZ() + 0.5D;
			if (client.player.squaredDistanceTo(x, y, z) > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE)
			{
				return;
			}
			double camX = camera.getPos().x;
			double camY = camera.getPos().y;
			double camZ = camera.getPos().z;
			RenderSystem.pushMatrix();
			RenderSystem.translatef((float)(x - camX), (float)(y - camY), (float)(z - camZ));
			RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
			RenderSystem.multMatrix(new Matrix4f(camera.getRotation()));
			RenderSystem.scalef(FONT_SIZE, -FONT_SIZE, FONT_SIZE);
			RenderSystem.enableTexture();
			RenderSystem.disableDepthTest();  // visibleThroughObjects
			RenderSystem.depthMask(true);
			RenderSystem.scalef(-1.0F, 1.0F, 1.0F);
			RenderSystem.enableAlphaTest();

			float totalWidth = 0.0F;
			for (String text: texts)
			{
				totalWidth += client.textRenderer.getStringWidth(text);
			}

			float writtenWidth = 0.0F;
			for (int i = 0; i < texts.length; i++)
			{
				VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
				float renderX = -totalWidth * 0.5F + writtenWidth;
				float renderY = client.textRenderer.getStringBoundedHeight(texts[i], Integer.MAX_VALUE) * (-0.5F + 1.25F * line);
				Matrix4f matrix4f = Rotation3.identity().getMatrix();
				client.textRenderer.draw(texts[i], renderX, renderY, colors[i], false, matrix4f, immediate, true, 0, 0xF000F0);
				immediate.draw();

				writtenWidth += client.textRenderer.getStringWidth(texts[i]);
			}

			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableDepthTest();
			RenderSystem.popMatrix();
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
		BlockView chunk = world.getChunkManager().getChunk(this.pos.getX() >> 4, this.pos.getZ() >> 4);
		if (chunk instanceof WorldChunk && !((WorldChunk)chunk).isEmpty())  // it's a real loaded chunk
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

			drawString(this.pos, tickDelta, -0.5F,String.format("%s %s", I18n.translate(actionKey), actionResult),goldValue);

			drawString(
					this.pos, tickDelta, 0.5F,
					new String[]{
							I18n.translate("pistorder.block_count.pre"),
							String.valueOf(this.movedBlocks.size()),
							I18n.translate("pistorder.block_count.post")
					},
					new int[]{goldValue, this.color, goldValue}
			);

			for (int i = 0; i < this.movedBlocks.size(); i++)
			{
				drawString(this.movedBlocks.get(i), tickDelta, 0.0F, String.valueOf(i + 1), this.color);
			}
			for (int i = 0; i < this.brokenBlocks.size(); i++)
			{
				drawString(this.brokenBlocks.get(i), tickDelta, 0.0F, String.valueOf(i + 1), Formatting.RED.getColorValue());
			}
		}
	}
}
