package me.fallenbreath.pistorder.pushlimit.handlers;

public class PistorderHandler implements PushLimitHandler
{
	private Integer currentPushLimit = 12;

	@Override
	public String getModId()
	{
		return null;
	}

	@Override
	public void setPushLimit(int pushLimit)
	{
		this.currentPushLimit = pushLimit;
	}

	@Override
	public int getPushLimit()
	{
		return this.currentPushLimit;
	}
}
