package me.fallenbreath.pistorder.utils;

import me.fallenbreath.pistorder.pushlimit.PushLimitManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class PistorderMixinPlugin implements IMixinConfigPlugin
{
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		// only loads our pushLimit modifier when carpet mod not loaded
		if (mixinClassName.endsWith(".PistonHandlerMixin"))
		{
			return PushLimitManager.getInstance().shouldLoadPistorderPushLimitMixin();
		}
		return true;
	}

	// dummy stuffs down below

	@Override
	public void onLoad(String mixinPackage)
	{
	}

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
	}

	@Override
	public List<String> getMixins()
	{
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}
}
