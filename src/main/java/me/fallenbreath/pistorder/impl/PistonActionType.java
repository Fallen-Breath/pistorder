package me.fallenbreath.pistorder.impl;

public enum PistonActionType
{
	PUSH,
	RETRACT;

	public boolean isPush()
	{
		return this == PUSH;
	}

	public boolean isRetract()
	{
		return this == RETRACT;
	}
}
