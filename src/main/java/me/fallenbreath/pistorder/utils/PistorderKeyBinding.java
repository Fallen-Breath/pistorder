package me.fallenbreath.pistorder.utils;

import com.google.common.collect.Lists;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

import java.util.List;

public class PistorderKeyBinding
{
	public static final KeyBinding CLEAR_DISPLAY_KEY = new KeyBinding("pistorder.clear_display", InputMappings.getInputByName("key.keyboard.o").getKeyCode(), "key.categories.misc");

	public static KeyBinding[] updateVanillaKeyBinding(KeyBinding[] keysAll)
	{
		List<KeyBinding> list = Lists.newArrayList(keysAll);
		list.add(CLEAR_DISPLAY_KEY);
		return list.toArray(new KeyBinding[0]);
	}
}
