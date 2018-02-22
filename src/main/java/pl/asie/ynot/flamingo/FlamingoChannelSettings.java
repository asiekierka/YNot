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

package pl.asie.ynot.flamingo;

import com.reddit.user.koppeh.flamingo.TileEntityFlamingo;
import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import mcjty.xnet.api.channels.IConnectorSettings;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.keys.SidedConsumer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.ynot.YNot;
import pl.asie.ynot.enums.InsertionMode;
import pl.asie.ynot.mekanism.GasConnectorSettings;
import pl.asie.ynot.traits.TraitedChannelSettings;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlamingoChannelSettings extends TraitedChannelSettings {
	private TObjectFloatMap<Pair<SidedConsumer, FlamingoConnectorSettings>> sources;
	private List<Pair<SidedConsumer, FlamingoConnectorSettings>> destinations;

	@Override
	public void tick(int channel, IControllerContext context) {
		updateCache(channel, context);
		TObjectFloatIterator<Pair<SidedConsumer, FlamingoConnectorSettings>> it = sources.iterator();
		Set<BlockPos> sourcePos = new HashSet<>();
		float power = 0.0f;
		while (it.hasNext()) {
			it.advance();
			Pair<SidedConsumer, FlamingoConnectorSettings> entry = it.key();
			BlockPos extractorPos = context.findConsumerPosition(entry.getKey().getConsumerId());
			if (extractorPos != null) {
				EnumFacing side = entry.getKey().getSide();
				BlockPos pos = extractorPos.offset(side);
				if (!shouldCheck(context, pos, extractorPos, entry.getValue())) {
					continue;
				}

				TileEntity tile = context.getControllerWorld().getTileEntity(pos);
				if (!(tile instanceof TileEntityFlamingo)) {
					continue;
				}

				float oldValue = it.value();
				float newValue = ((TileEntityFlamingo) tile).getWiggleStrength();
				it.setValue(newValue);
				if (newValue > oldValue) {
					power += newValue;
					sourcePos.add(pos);
				}
			}
		}

		if (power > 0.0f) {
			List<BlockPos> s = destinations.stream().map(entry -> {
				BlockPos extractorPos = context.findConsumerPosition(entry.getKey().getConsumerId());
				if (extractorPos != null) {
					EnumFacing side = entry.getKey().getSide();
					BlockPos pos = extractorPos.offset(side);

					if (sourcePos.contains(pos)) {
						return null;
					}

					if (!shouldCheck(context, pos, extractorPos, entry.getValue())) {
						return null;
					}

					return pos;
				} else {
					return null;
				}
			}).filter(Objects::nonNull).collect(Collectors.toList());
			final float ultimatePower = Math.min(power / s.size(), 40);
			s.forEach(entry -> context.getControllerWorld().addBlockEvent(entry, context.getControllerWorld().getBlockState(entry).getBlock(),
					(int) ultimatePower,
					(int) ((ultimatePower - Math.floor(ultimatePower)) * 256)));
		}
	}

	private void updateCache(int channel, IControllerContext context) {
		if (sources == null) {
			sources = new TObjectFloatHashMap<>();
			destinations = new ArrayList<>();

			Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
			for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
				FlamingoConnectorSettings con = (FlamingoConnectorSettings) entry.getValue();
				if (con.insertionMode.get() != InsertionMode.INS) {
					sources.put(Pair.of(entry.getKey(), con), 0);
				}

				if (con.insertionMode.get() != InsertionMode.EXT) {
					destinations.add(Pair.of(entry.getKey(), con));
				}
			}

			connectors = context.getRoutedConnectors(channel);
			for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
				FlamingoConnectorSettings con = (FlamingoConnectorSettings) entry.getValue();
				if (con.insertionMode.get() != InsertionMode.EXT) {
					destinations.add(Pair.of(entry.getKey(), con));
				}
			}
		}
	}

	@Override
	public void cleanCache() {
		sources = null;
		destinations = null;
	}

	@Override
	public int getColors() {
		return 0;
	}

	@Nullable
	@Override
	public IndicatorIcon getIndicatorIcon() {
		return new IndicatorIcon(YNot.iconGui, 11, 0, 11, 10);
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

	}
}
