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

package me.fallenbreath.pistorder.pushlimit;

import com.google.common.collect.Lists;
import me.fallenbreath.pistorder.pushlimit.handlers.FabricCarpetHandler;
import me.fallenbreath.pistorder.pushlimit.handlers.PushLimitHandler;
import me.fallenbreath.pistorder.pushlimit.handlers.QuickCarpetHandler;
import me.fallenbreath.pistorder.pushlimit.handlers.VanillaHandler;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;
import java.util.function.Supplier;

public class PushLimitManager
{
	private static final PushLimitManager INSTANCE = new PushLimitManager();

	private Integer oldPushLimit;
	private final List<PushLimitHandler> handlers = Lists.newArrayList();

	private PushLimitManager()
	{
		this.oldPushLimit = null;

		// mods that modify the push limit
		this.addPushLimitHandler(QuickCarpetHandler::new);
		this.addPushLimitHandler(FabricCarpetHandler::new);

		// leave the fallback handler to the end
		this.addPushLimitHandler(VanillaHandler::new);
	}

	private void addPushLimitHandler(Supplier<PushLimitHandler> handlerSupplier)
	{
		try
		{
			PushLimitHandler handler = handlerSupplier.get();
			this.handlers.add(handler);
		}
		catch (Throwable ignored)
		{
		}
	}

	public static PushLimitManager getInstance()
	{
		return INSTANCE;
	}

	public boolean shouldLoadPistorderPushLimitMixin()
	{
		for (PushLimitHandler handler: this.handlers)
		{
			String modId = handler.getModId();
			if (modId != null && FabricLoader.getInstance().isModLoaded(modId))
			{
				return false;
			}
		}
		return true;
	}

	public int getPushLimit()
	{
		for (PushLimitHandler handler: this.handlers)
		{
			return handler.getPushLimit();
		}
		throw new IllegalStateException("getPushLimit failed");
	}

	private void setPushLimit(int pushLimit)
	{
		for (PushLimitHandler handler: this.handlers)
		{
			handler.setPushLimit(pushLimit);
			return;
		}
		throw new IllegalStateException("setPushLimit failed");
	}

	public void overwritePushLimit(int pushLimit)
	{
		this.oldPushLimit = this.getPushLimit();
		this.setPushLimit(pushLimit);
	}

	public void restorePushLimit()
	{
		if (this.oldPushLimit != null)
		{
			this.setPushLimit(this.oldPushLimit);
			this.oldPushLimit = null;
		}
	}
}
