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

import com.reddit.user.koppeh.flamingo.TileEntityFlamingo;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.SidedEnvironment;
import mcjty.xnet.XNet;
import mcjty.xnet.api.IXNet;
import mcjty.xnet.api.channels.IConnectable;
import mekanism.api.gas.IGasHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pl.asie.ynot.flamingo.FlamingoChannelType;
import pl.asie.ynot.mekanism.GasChannelType;
import pl.asie.ynot.oc.OCChannelType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

@Mod(
		modid = "ynot",
		name = "YNot",
		version = "@VERSION@",
		dependencies = "required-after:xnet@[1.6.6,)",
		updateJSON = "http://asie.pl/files/minecraft/update/ynot.json"
)
public class YNot {
	public static final ResourceLocation iconGui = new ResourceLocation("ynot", "textures/gui/gui.png");
	private static Configuration config;

	public static int maxGasRateAdvanced, maxGasRateNormal;
	private static boolean enableMekanismGas, enableWiggles, enableOC;

	@CapabilityInject(IGasHandler.class)
	private static Capability<IGasHandler> capMekGasHandler;
	@CapabilityInject(Environment.class)
	private static Capability<Environment> capOCEnv;
	@CapabilityInject(SidedEnvironment.class)
	private static Capability<SidedEnvironment> capOCSidedEnv;

	public static class XNetHook implements Function<IXNet, Void> {
		@Override
		public Void apply(IXNet xNet) {
			boolean needsCustomConnections = false;

			if (enableMekanismGas) {
				xNet.registerChannelType(new GasChannelType());
				YNotConnectable.add(capMekGasHandler);
				YNotConnectable.add(capMekGasHandler.getClass());
				needsCustomConnections = true;
			}

			if (enableWiggles) {
				FlamingoChannelType type = new FlamingoChannelType();
				xNet.registerChannelType(type);
				xNet.registerConnectable(type);
			}

			if (enableOC) {
				xNet.registerChannelType(new OCChannelType());
				YNotConnectable.add(capOCEnv, capOCSidedEnv);
				YNotConnectable.add(capOCEnv.getClass(), capOCSidedEnv.getClass());
				needsCustomConnections = true;
			}

			if (needsCustomConnections) {
				xNet.registerConnectable(YNotConnectable.INSTANCE);
			}

			return null;
		}
	}

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());

		if (Loader.isModLoaded("mekanism")) {
			enableMekanismGas = config.getBoolean("mekanismGasChannel", "features", true, "Mekanism Gas Channel support for XNet");

			// Mekanism's balance: 256 (gas) -> 1000 (fluid), so let's keep with that as a default.
			maxGasRateAdvanced = config.getInt("mekanismGasMaxRateAdvanced", "balance", 256 * 5, 1, Integer.MAX_VALUE, "Maximum transfer rate for Mekanism Gas and advanced connectors");
			maxGasRateNormal = config.getInt("mekanismGasMaxRateNormal", "balance", 256, 1, Integer.MAX_VALUE, "Maximum transfer rate for Mekanism Gas and normal connectors");
		}

		if (Loader.isModLoaded("flamingo")) {
			enableWiggles = config.getBoolean("flamingoWiggles", "features", true, "Don't question it.");
		}

		if (Loader.isModLoaded("opencomputers")) {
			enableOC = config.getBoolean("opencomputersCable", "features", true, "Adds the ability to send network messages and use components.");
		}

		if (config.hasChanged()) {
			config.save();
		}
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		FMLInterModComms.sendFunctionMessage("xnet", "getXNet", "pl.asie.ynot.YNot$XNetHook");
	}
}
