package me.fallenbreath.pistorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public class PistorderMod implements InitializationListener
{
	public static final String MOD_NAME = "Pistorder";
	public static final String MOD_ID = "pistorder";

	public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialization()
	{
		MixinBootstrap.init();
		Mixins.addConfiguration("pistorder.mixins.json");
	}
}
