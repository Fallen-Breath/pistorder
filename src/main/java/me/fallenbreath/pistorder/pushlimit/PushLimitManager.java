package me.fallenbreath.pistorder.pushlimit;

import com.google.common.collect.Lists;
import me.fallenbreath.pistorder.pushlimit.handlers.FabricCarpetHandler;
import me.fallenbreath.pistorder.pushlimit.handlers.PistorderHandler;
import me.fallenbreath.pistorder.pushlimit.handlers.PushLimitHandler;
import me.fallenbreath.pistorder.pushlimit.handlers.QuickCarpetHandler;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class PushLimitManager
{
	private static final PushLimitManager INSTANCE = new PushLimitManager();

	private Integer oldPushLimit;
	private final List<PushLimitHandler> handlers = Lists.newArrayList();

	private PushLimitManager()
	{
		this.oldPushLimit = null;

		// mods that modifies the push limit
		this.handlers.add(new QuickCarpetHandler());
		this.handlers.add(new FabricCarpetHandler());

		// leave the fallback handler to the end
		this.handlers.add(new PistorderHandler());
	}

	public static PushLimitManager getInstance()
	{
		return INSTANCE;
	}

	public boolean showLoadPistorderPushLimitMixin()
	{
		for (PushLimitHandler handler: this.handlers)
		{
			String modId = handler.getModId();
			if (modId != null && FabricLoader.getInstance().isModLoaded(modId))
			{
				return false;
			}
		}
		return true;
	}

	public int getPushLimit()
	{
		for (PushLimitHandler handler: this.handlers)
		{
			try
			{
				return handler.getPushLimit();
			}
			catch (NoClassDefFoundError ignored)
			{
			}
		}
		throw new IllegalStateException("getPushLimit failed");
	}

	public void setPushLimit(int pushLimit)
	{
		for (PushLimitHandler handler: this.handlers)
		{
			try
			{
				handler.setPushLimit(pushLimit);
				return;
			}
			catch (NoClassDefFoundError ignored)
			{
			}
		}
		throw new IllegalStateException("setPushLimit failed");
	}

	public void overwritePushLimit(int pushLimit)
	{
		this.oldPushLimit = this.getPushLimit();
		this.setPushLimit(pushLimit);
	}

	public void restorePushLimit()
	{
		if (this.oldPushLimit != null)
		{
			this.setPushLimit(this.oldPushLimit);
			this.oldPushLimit = null;
		}
	}
}
