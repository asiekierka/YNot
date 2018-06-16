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

import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.blocks.controller.gui.GuiController;
import net.minecraft.util.EnumFacing;
import pl.asie.ynot.YNot;
import pl.asie.ynot.enums.InsertionMode;
import pl.asie.ynot.traits.TraitEnum;
import pl.asie.ynot.traits.TraitedConnectorSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlamingoConnectorSettings extends TraitedConnectorSettings {
	protected TraitEnum<InsertionMode> insertionMode;

	public FlamingoConnectorSettings(@Nonnull EnumFacing side) {
		super(side);
		register(insertionMode = new TraitEnum<>("mode", InsertionMode.class, InsertionMode.INS));
	}

	@Nullable
	@Override
	public IndicatorIcon getIndicatorIcon() {
		switch (insertionMode.get()) {
			case INS:
				return new IndicatorIcon(iconXnetGuiElements, 0, 70, 13, 10);
			case EXT:
			default:
				return new IndicatorIcon(iconXnetGuiElements, 13, 70, 13, 10);
		}
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
		colorsGui(gui);
		redstoneGui(gui);
		gui.nl();
		insertionMode.apply("Insert or extract mode", gui);
	}
}
