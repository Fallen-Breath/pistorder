package me.fallenbreath.pistorder;

import me.fallenbreath.pistorder.mixins.PistonBlockAccessor;
import me.fallenbreath.pistorder.pushlimit.PushLimitManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
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
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Pistorder
{
	private static final Pistorder INSTANCE = new Pistorder();
	private static final double MAX_RENDER_DISTANCE = 256.0D;
	private static final int MAX_PUSH_LIMIT_FOR_CALC = 128;
	private static final float FONT_SIZE = 0.025F;

	private ClickInfo info = null;
	private List<BlockPos> movedBlocks;
	private List<BlockPos> brokenBlocks;
	private boolean moveSuccess;

	public static Pistorder getInstance()
	{
		return INSTANCE;
	}

	public EnumActionResult onPlayerRightClickBlock(World world, EntityPlayer player, EnumHand hand, BlockPos pos)
	{
		// click with empty main hand, not sneaking
		if (hand == EnumHand.MAIN_HAND && player.getHeldItemMainhand().isEmpty() && !player.isSneaking())
		{
			IBlockState blockState = world.getBlockState(pos);
			Block block = blockState.getBlock();
			if (block instanceof BlockPistonBase)
			{
				boolean extended = blockState.get(BlockPistonBase.EXTENDED);
				if (!extended || ((PistonBlockAccessor)block).getIsSticky())
				{
					this.click(world, pos, blockState.get(BlockStateProperties.FACING), extended ? ActionType.RETRACT : ActionType.PUSH);
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.FAIL;
	}

	public boolean isEnabled()
	{
		return this.info != null;
	}

	synchronized private void click(World world, BlockPos pos, EnumFacing pistonFacing, ActionType actionType)
	{
		ClickInfo newInfo = new ClickInfo(world, pos, pistonFacing, actionType);
		if (newInfo.equals(this.info))
		{
			this.info = null;
		}
		else
		{
			this.info = newInfo;

			IBlockState[] states = new IBlockState[2];
			if (actionType.isRetract())
			{
				states[0] = world.getBlockState(pos);  // piston base
				states[1] = world.getBlockState(pos.offset(pistonFacing));  // piston head
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
				world.setBlockState(pos.offset(pistonFacing), Blocks.AIR.getDefaultState(), 18);
			}

			BlockPistonStructureHelper pistonHandler = new BlockPistonStructureHelper(world, pos, pistonFacing, actionType.isPush());
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

			if (actionType.isRetract())
			{
				world.setBlockState(pos, states[0], 18);
				world.setBlockState(pos.offset(pistonFacing), states[1], 18);
			}
		}
	}

	/**
	 * Stolen from {@link DebugRenderer#renderDebugText(String, double, double, double, float, int)}
	 */
	public static void drawString(String text, BlockPos pos, float tickDelta, int color, float line)
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

			float renderX = -client.fontRenderer.getStringWidth(text) * 0.5F;
			float renderY = client.fontRenderer.getWordWrappedHeight(text, Integer.MAX_VALUE) * (-0.5F + 1.25F * line);
			client.fontRenderer.drawString(text, renderX, renderY, color);

			GlStateManager.enableLighting();
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.popMatrix();
		}
	}

	@SuppressWarnings("ConstantConditions")
	public void render(float tickDelta)
	{
		if (this.isEnabled())
		{
			Minecraft client = Minecraft.getInstance();
			if (this.info.world.equals(client.world))
			{
				String actionKey = this.info.actionType.isPush() ? "pistorder.push" : "pistorder.retract";
				String actionResult = this.moveSuccess ? TextFormatting.GREEN + "√" : TextFormatting.RED + "×";
				drawString(String.format("%s %s", I18n.format(actionKey), actionResult), this.info.pos, tickDelta, TextFormatting.GOLD.getColor(), -0.5F);
				drawString(I18n.format("pistorder.block_count", this.movedBlocks.size()), this.info.pos, tickDelta, TextFormatting.GOLD.getColor(), 0.5F);

				for (int i = 0; i < this.movedBlocks.size(); i++)
				{
					drawString(String.valueOf(i + 1), this.movedBlocks.get(i), tickDelta, TextFormatting.WHITE.getColor(), 0);
				}
				for (int i = 0; i < this.brokenBlocks.size(); i++)
				{
					drawString(String.valueOf(i + 1), this.brokenBlocks.get(i), tickDelta, TextFormatting.RED.getColor() | (0xFF << 24), 0);
				}
			}
		}
	}

	public static class ClickInfo
	{
		public final World world;
		public final BlockPos pos;
		public final EnumFacing direction;
		public final ActionType actionType;

		public ClickInfo(World world, BlockPos pos, EnumFacing direction, ActionType actionType)
		{
			this.world = world;
			this.pos = pos;
			this.direction = direction;
			this.actionType = actionType;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof ClickInfo)) return false;
			ClickInfo that = (ClickInfo) o;
			return Objects.equals(world, that.world) &&
					Objects.equals(pos, that.pos) &&
					direction == that.direction &&
					actionType == that.actionType;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(world, pos, direction, actionType);
		}
	}

	public enum ActionType
	{
		PUSH,
		RETRACT;

		public boolean isPush()
		{
			return this == ActionType.PUSH;
		}

		public boolean isRetract()
		{
			return this == ActionType.RETRACT;
		}
	}
}
