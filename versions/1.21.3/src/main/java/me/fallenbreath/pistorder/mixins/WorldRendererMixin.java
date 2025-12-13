/*
 * This file is part of the Pistorder project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2024  Fallen_Breath and contributors
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

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.pistorder.impl.Pistorder;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin
{
	@Inject(
			// lambda method in addLateDebugPass
			//#if MC >= 1.21.11
			//$$ method = "method_75413",
			//#elseif MC >= 1.21.9
			//$$ method = "method_72915",
			//#else
			method = "method_62212",
			//#endif
			at = @At(
					//#if MC >= 1.21.11
					//$$ value = "FIELD",
					//$$ target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizedGizmos:Lnet/minecraft/client/renderer/LevelRenderer$FinalizedGizmos;",
					//$$ ordinal = 0
					//#else

					value = "INVOKE",
					//#if MC >= 1.21.9
					//$$ target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDZ)V"
					//#else
					target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;renderAfterTranslucents(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V"
					//#endif

					//#endif
			)
	)
	private void renderPistorder(
			CallbackInfo ci,
			@Local PoseStack matrices
	)
	{
		Pistorder.getInstance().render(
				matrices,
				0  // actually this is unused
		);
	}
}
