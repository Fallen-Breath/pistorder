package me.fallenbreath.pistorder.pushlimit.handlers;

import carpet.CarpetSettings;

public class FabricCarpetHandler implements PushLimitHandler
{
	@Override
	public String getModId()
	{
		return "carpet";
	}

	@Override
	public void setPushLimit(int pushLimit)
	{
		CarpetSettings.pushLimit = pushLimit;
	}

	@Override
	public int getPushLimit()
	{
		return CarpetSettings.pushLimit;
	}
}
