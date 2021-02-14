package me.fallenbreath.pistorder;

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
