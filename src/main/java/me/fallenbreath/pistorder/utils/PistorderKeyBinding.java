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
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.List;

public class PistorderKeyBinding
{
	public static final KeyMapping CLEAR_DISPLAY_KEY = new KeyMapping(
			"pistorder.clear_display",
			InputConstants.getKey("key.keyboard.p").getValue(),
			//#if MC >= 1.21.9
			//$$ KeyMapping.Category.MISC
			//#else
			"key.categories.misc"
			//#endif
	);

	public static KeyMapping[] updateVanillaKeyBinding(KeyMapping[] keysAll)
	{
		List<KeyMapping> list = Lists.newArrayList(keysAll);
		list.remove(CLEAR_DISPLAY_KEY);
		list.add(CLEAR_DISPLAY_KEY);
		return list.toArray(new KeyMapping[0]);
	}
}
