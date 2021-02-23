package me.fallenbreath.pistorder.utils;

import com.google.common.collect.Lists;
import me.fallenbreath.pistorder.PistorderMod;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.util.List;

public class PistorderKeyBinding
{
	public static final KeyBinding CLEAR_DISPLAY_KEY = new KeyBinding(
			(new Identifier(PistorderMod.MOD_ID, "clear")).toString(),
			InputUtil.fromName("key.keyboard.o").getKeyCode(),
			"key.categories.misc"
	);

	public static KeyBinding[] updateVanillaKeyBinding(KeyBinding[] keysAll)
	{
		List<KeyBinding> list = Lists.newArrayList(keysAll);
		list.add(CLEAR_DISPLAY_KEY);
		return list.toArray(new KeyBinding[0]);
	}
}
