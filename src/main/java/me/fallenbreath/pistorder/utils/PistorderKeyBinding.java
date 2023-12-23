/*
 * This file is part of the Pistorder project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
 *
 * Pistorder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pistorder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pistorder.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.pistorder.utils;

import com.google.common.collect.Lists;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.List;

public class PistorderKeyBinding
{
	public static final KeyBinding CLEAR_DISPLAY_KEY = new KeyBinding("pistorder.clear_display", InputUtil.fromTranslationKey("key.keyboard.p").getCode(), "key.categories.misc");

	public static KeyBinding[] updateVanillaKeyBinding(KeyBinding[] keysAll)
	{
		List<KeyBinding> list = Lists.newArrayList(keysAll);
		list.remove(CLEAR_DISPLAY_KEY);
		list.add(CLEAR_DISPLAY_KEY);
		return list.toArray(new KeyBinding[0]);
	}
}
