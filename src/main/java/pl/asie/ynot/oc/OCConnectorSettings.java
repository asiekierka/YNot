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

package pl.asie.ynot.oc;

import li.cil.oc.api.network.Environment;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.blocks.controller.gui.GuiController;
import net.minecraft.util.EnumFacing;
import pl.asie.ynot.YNot;
import pl.asie.ynot.enums.InsertionMode;
import pl.asie.ynot.enums.OCNetworkMode;
import pl.asie.ynot.traits.TraitEnum;
import pl.asie.ynot.traits.TraitedConnectorSettings;

import javax.annotation.Nullable;

public class OCConnectorSettings extends TraitedConnectorSettings {
    TraitEnum<OCNetworkMode> networkMode;

    OCConnectorSettings(EnumFacing side) {
        super(side);

        register(networkMode = new TraitEnum<>("mode", OCNetworkMode.class, OCNetworkMode.NETWORK_ONLY));
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        switch (networkMode.get()) {
            case COMPONENT_AND_NETWORK:
                return new IndicatorIcon(YNot.iconGui, 26, 10, 13, 10);
            case NETWORK_ONLY:
            default:
                return new IndicatorIcon(YNot.iconGui, 13, 10, 13, 10);
        }
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    @Override
    public boolean isEnabled(String tag) {
        /* if (tag.equals(TAG_FACING)) {
            return advanced;
        } */
        return true;
    }

    @Override
    public void createGui(IEditorGui gui) {
        /* advanced = gui.isAdvanced();
        sideGui(gui); */
        networkMode.apply("Components + Network or Network only.", gui);
    }
}
