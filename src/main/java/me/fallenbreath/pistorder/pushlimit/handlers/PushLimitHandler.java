package me.fallenbreath.pistorder.pushlimit.handlers;

public interface PushLimitHandler
{
	String getModId();

	void setPushLimit(int pushLimit);

	int getPushLimit();
}
