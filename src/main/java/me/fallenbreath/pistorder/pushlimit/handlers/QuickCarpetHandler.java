package me.fallenbreath.pistorder.pushlimit.handlers;

import quickcarpet.settings.Settings;

public class QuickCarpetHandler implements PushLimitHandler
{
	@Override
	public String getModId()
	{
		return "quickcarpet";
	}

	@Override
	public void setPushLimit(int pushLimit)
	{
		Settings.pushLimit = pushLimit;
	}

	@Override
	public int getPushLimit()
	{
		return Settings.pushLimit;
	}
}
