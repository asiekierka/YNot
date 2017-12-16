/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of YNot.
 *
 * YNot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * YNot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with YNot.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.ynot;

import mcjty.xnet.XNet;
import mcjty.xnet.api.IXNet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import pl.asie.ynot.mekanism.GasChannelType;

import java.util.function.Function;

@Mod(
		modid = "ynot",
		name = "YNot",
		version = "@VERSION@",
		dependencies = "required-after:xnet@[1.5.0,)",
		updateJSON = "http://asie.pl/files/minecraft/update/ynot.json"
)
public class YNot {
	public static final ResourceLocation iconGui = new ResourceLocation("ynot", "textures/gui/gui.png");

	public static class XNetGetter implements Function<IXNet, Void> {
		@Override
		public Void apply(IXNet xNet) {
			if (Loader.isModLoaded("mekanism")) {
				xNet.registerChannelType(new GasChannelType());
			}
			return null;
		}
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		FMLInterModComms.sendFunctionMessage("xnet", "getXNet", "pl.asie.ynot.YNot$XNetGetter");
	}
}
