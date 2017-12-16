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

package pl.asie.ynot.mekanism;

import mcjty.lib.varia.WorldTools;
import mcjty.xnet.api.channels.IConnectorSettings;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.keys.SidedConsumer;
import mcjty.xnet.blocks.controller.gui.GuiController;
import mcjty.xnet.config.GeneralConfiguration;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import pl.asie.ynot.YNot;
import pl.asie.ynot.enums.InsertionMode;
import pl.asie.ynot.enums.LiquidTransferMode;
import pl.asie.ynot.traits.TraitEnum;
import pl.asie.ynot.traits.TraitedChannelSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GasChannelSettings extends TraitedChannelSettings {
	@CapabilityInject(IGasHandler.class)
	private static Capability<IGasHandler> GAS_HANDLER_CAPABILITY;

	private TraitEnum<LiquidTransferMode> transferMode;
	private Map<SidedConsumer, GasConnectorSettings> gasExtractors = null;
	private List<Pair<SidedConsumer, GasConnectorSettings>> gasConsumers = null;
	private int roundRobinOffset = 0;
	private int delay = 0;

	public GasChannelSettings() {
		super();
		register(transferMode = new TraitEnum<>("mode", LiquidTransferMode.class, LiquidTransferMode.DISTRIBUTE));
	}

	private boolean shouldCheck(IControllerContext context, BlockPos pos, GasConnectorSettings settings) {
		World world = context.getControllerWorld();
		if (!WorldTools.chunkLoaded(world, pos)) {
			return false;
		}
		if (checkRedstone(world, settings, pos)) {
			return false;
		}
		if (!context.matchColor(settings.getColorsMask())) {
			return false;
		}
		return true;
	}

	public static @Nullable IGasHandler getGasHandler(TileEntity tile, EnumFacing facing) {
		if (tile instanceof IGasHandler) {
			return (IGasHandler) tile;
		} else if (tile != null && tile.hasCapability(GAS_HANDLER_CAPABILITY, facing)) {
			return tile.getCapability(GAS_HANDLER_CAPABILITY, facing);
		} else {
			return null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		delay = tag.getInteger("delay");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("delay", delay);
	}

	@Override
	public void tick(int channel, IControllerContext context) {
		updateCache(channel, context);

		delay--;
		while (delay <= 0) {
			delay += 200*6;
		}
		if (delay % 10 != 0) {
			return;
		}

		// @todo optimize
		World world = context.getControllerWorld();
		for (Map.Entry<SidedConsumer, GasConnectorSettings> entry : gasExtractors.entrySet()) {
			GasConnectorSettings settings = entry.getValue();
			GasStack filterGas = settings.getFilter();
			if (delay % settings.speeds.get() != 0) {
				continue;
			}

			BlockPos extractorPos = context.findConsumerPosition(entry.getKey().getConsumerId());
			if (extractorPos != null) {
				EnumFacing side = entry.getKey().getSide();
				BlockPos pos = extractorPos.offset(side);
				if (!shouldCheck(context, pos, settings)) {
					continue;
				}

				IGasHandler handler = getGasHandler(world.getTileEntity(pos), settings.getFacing());
				if (handler != null) {
					int amount = settings.getRate();
					GasStack gas = handler.drawGas(settings.getFacing(), amount, false);
					if (gas != null) {
						if (filterGas != null && !filterGas.isGasEqual(gas)) {
							continue;
						}

						List<Triple<SidedConsumer, GasConnectorSettings, Integer>> inserted = new ArrayList<>();
						int remaining = insertGasSimulate(inserted, context, gas);
						if (!inserted.isEmpty()) {
							if (context.checkAndConsumeRF(GeneralConfiguration.controllerOperationRFT)) {
								gas = handler.drawGas(settings.getFacing(), gas.amount - remaining, true);
								insertGasReal(context, inserted, gas);
							}
						}
					}
				}
			}
		}
	}

	private void insertGasReal(IControllerContext context, List<Triple<SidedConsumer, GasConnectorSettings, Integer>> inserted, GasStack stack) {
		int amount = stack.amount;
		for (Triple<SidedConsumer, GasConnectorSettings, Integer> pair : inserted) {
			BlockPos consumerPosition = context.findConsumerPosition(pair.getLeft().getConsumerId());
			EnumFacing side = pair.getLeft().getSide();
			GasConnectorSettings settings = pair.getMiddle();
			GasStack filterGas = settings.getFilter();
			if (filterGas != null && !filterGas.isGasEqual(stack)) {
				continue;
			}

			BlockPos pos = consumerPosition.offset(side);
			IGasHandler handler = getGasHandler(context.getControllerWorld().getTileEntity(pos), settings.getFacing());
			if (handler != null) {
				int toinsert = Math.min(settings.getRate(), amount);
				GasStack copy = stack.copy();
				copy.amount = toinsert;

				int filled = handler.receiveGas(settings.getFacing(), copy, true);
				if (filled > 0) {
					roundRobinOffset = (pair.getRight() + 1) % gasConsumers.size();
					amount -= filled;
					if (amount <= 0) {
						return;
					}
				}
			}
		}
	}

	private int insertGasSimulate(@Nonnull List<Triple<SidedConsumer, GasConnectorSettings, Integer>> inserted, @Nonnull IControllerContext context, @Nonnull GasStack stack) {
		World world = context.getControllerWorld();
		if (transferMode.get() == LiquidTransferMode.PRIORITY) {
			roundRobinOffset = 0;
		}
		int amount = stack.amount;
		for (int j = 0; j < gasConsumers.size(); j++) {
			int i = (j + roundRobinOffset) % gasConsumers.size();
			Pair<SidedConsumer, GasConnectorSettings> entry = gasConsumers.get(i);
			GasConnectorSettings settings = entry.getValue();

			BlockPos consumerPos = context.findConsumerPosition(entry.getKey().getConsumerId());
			if (consumerPos != null && shouldCheck(context, consumerPos, entry.getValue())) {
				EnumFacing side = entry.getKey().getSide();
				BlockPos pos = consumerPos.offset(side);
				IGasHandler handler = getGasHandler(world.getTileEntity(pos), settings.getFacing());
				if (handler != null) {
					if (!handler.canReceiveGas(settings.getFacing(), stack.getGas())) {
						continue;
					}

					int toinsert = Math.min(settings.getRate(), amount);
					GasStack copy = stack.copy();
					copy.amount = toinsert;

					int filled = handler.receiveGas(settings.getFacing(), copy, false);
					if (filled > 0) {
						inserted.add(Triple.of(entry.getLeft(), entry.getRight(), i));
						amount -= filled;
						if (amount <= 0) {
							return 0;
						}
					}
				}
			}
		}
		return amount;
	}

	@Override
	public void cleanCache() {
		gasExtractors = null;
		gasConsumers = null;
	}

	@Override
	public int getColors() {
		return 0;
	}

	@Nullable
	@Override
	public IndicatorIcon getIndicatorIcon() {
		return new IndicatorIcon(YNot.iconGui, 0, 0, 11, 10);
	}

	@Nullable
	@Override
	public String getIndicator() {
		return null;
	}

	@Override
	public boolean isEnabled(String tag) {
		return true;
	}

	@Override
	public void createGui(IEditorGui gui) {
		gui.nl();
		transferMode.apply("Gas distribution mode", gui);
	}

	private void updateCache(int channel, IControllerContext context) {
		if (gasExtractors == null) {
			gasExtractors = new HashMap<>();
			gasConsumers = new ArrayList<>();
			Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
			for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
				GasConnectorSettings con = (GasConnectorSettings) entry.getValue();
				if (con.insertionMode.get() == InsertionMode.EXT) {
					gasExtractors.put(entry.getKey(), con);
				} else {
					gasConsumers.add(Pair.of(entry.getKey(), con));
				}
			}

			connectors = context.getRoutedConnectors(channel);
			for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
				GasConnectorSettings con = (GasConnectorSettings) entry.getValue();
				if (con.insertionMode.get() == InsertionMode.INS) {
					gasConsumers.add(Pair.of(entry.getKey(), con));
				}
			}

			gasConsumers.sort((o1, o2) -> o2.getRight().priority.get().compareTo(o1.getRight().priority.get()));
		}
	}
}
