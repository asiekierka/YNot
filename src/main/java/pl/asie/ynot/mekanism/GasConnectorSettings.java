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

import com.google.common.collect.ImmutableSet;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.blocks.controller.gui.GuiController;
import mcjty.xnet.config.GeneralConfiguration;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.ynot.enums.InsertionMode;
import pl.asie.ynot.traits.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class GasConnectorSettings extends TraitedConnectorSettings {
	private static final int[] SPEEDS_NORMAL = {20, 60, 100, 200};
	private static final int[] SPEEDS_ADVANCED = {10, 20, 60, 100, 200};

	protected TraitEnum<InsertionMode> insertionMode;
	protected TraitFilterStack filterStack;
	protected TraitInteger priority, rate;
	protected TraitIntegerChoices speeds;

	public GasConnectorSettings(@Nonnull EnumFacing side) {
		super(side);
		advanced = true;
		register(insertionMode = new TraitEnum<>("mode", InsertionMode.class, InsertionMode.INS));
		register(filterStack = new TraitFilterStack("filter"));
		register(priority = new TraitInteger("priority", 0));
		register(rate = new TraitInteger("rate", null) {
			@Override
			public void update(Map<String, Object> data) {
				super.update(data);
				int maxRate = advanced ? GeneralConfiguration.maxFluidRateAdvanced : GeneralConfiguration.maxFluidRateNormal;
				if (val != null && val > maxRate) {
					val = maxRate;
				}
			}
		});
		register(speeds = new TraitIntegerChoices("speed", SPEEDS_ADVANCED));
	}

	public int getRate() {
		return rate.get() != null ? rate.get() : GeneralConfiguration.maxFluidRateNormal;
	}

	private void updateTraits() {
		speeds.setChoices(advanced ? SPEEDS_ADVANCED : SPEEDS_NORMAL);
	}

	public @Nullable GasStack getFilter() {
		ItemStack filter = filterStack.get();
		if (filter.getItem() instanceof IGasItem) {
			IGasItem item = (IGasItem) filter.getItem();
			return item.getGas(filter);
		} else {
			return null;
		}
	}

	@Nullable
	@Override
	public IndicatorIcon getIndicatorIcon() {
		switch (insertionMode.get()) {
			case INS:
				return new IndicatorIcon(GuiController.iconGuiElements, 0, 70, 13, 10);
			case EXT:
				return new IndicatorIcon(GuiController.iconGuiElements, 13, 70, 13, 10);
		}
		return null;
	}

	@Nullable
	@Override
	public String getIndicator() {
		return null;
	}

	private static final Set<String> INSERT_TAGS = ImmutableSet.of("mode", "filter", "priority", "rate", TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3");
	private static final Set<String> EXTRACT_TAGS = ImmutableSet.of("mode", "filter", "priority", "rate", "speed", TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3");

	@Override
	public boolean isEnabled(String tag) {
		if (tag.equals(TAG_FACING)) {
			return insertionMode.get() == InsertionMode.INS && advanced;
		}
		switch (insertionMode.get()) {
			case INS:
				return INSERT_TAGS.contains(tag);
			default:
				return EXTRACT_TAGS.contains(tag);
		}
	}

	@Override
	public void createGui(IEditorGui gui) {
		advanced = gui.isAdvanced();
		updateTraits();
		int maxRate = advanced ? GeneralConfiguration.maxFluidRateAdvanced : GeneralConfiguration.maxFluidRateNormal;

		sideGui(gui);
		colorsGui(gui);
		redstoneGui(gui);
		gui.nl();
		insertionMode.apply("Insert or extract mode", gui);
		speeds.apply("Number of ticks for each operation", gui);
		gui.nl().label("PRI");
		priority.apply("Insertion priority", gui);
		gui.nl().label("Rate");
		rate.apply((insertionMode.get() == InsertionMode.EXT ? "Gas extraction rate" : "Gas insertion rate") + "|(max " + maxRate + ")", gui);
		gui.nl().label("Filter");
		filterStack.apply("Filter stack", gui);
	}
}
