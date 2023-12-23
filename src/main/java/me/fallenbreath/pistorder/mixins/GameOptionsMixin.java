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

package me.fallenbreath.pistorder.mixins;

import me.fallenbreath.pistorder.utils.PistorderConfigure;
import me.fallenbreath.pistorder.utils.PistorderKeyBinding;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin
{
	@Mutable
	@Shadow @Final public KeyBinding[] allKeys;

	@Inject(method = "load", at = @At("HEAD"))
	public void loadPistorderConfigure(CallbackInfo info)
	{
		PistorderConfigure.load();
		PistorderConfigure.save();
		this.allKeys = PistorderKeyBinding.updateVanillaKeyBinding(this.allKeys);
	}

	@Inject(method = "write", at = @At("HEAD"))
	public void savePistorderConfigure(CallbackInfo info)
	{
		PistorderConfigure.save();
	}
}
