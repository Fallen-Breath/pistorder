package me.fallenbreath.pistorder;

import me.fallenbreath.pistorder.pushlimit.PushLimitManager;
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
		if (mixinClassName.endsWith(".BlockPistonStructureHelperMixin"))
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
		// necessary for rift
		return "pistorder.mixins.refmap.json";
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
	public void preApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}

	@Override
	public void postApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}
}
