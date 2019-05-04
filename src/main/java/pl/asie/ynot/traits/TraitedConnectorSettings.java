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

package pl.asie.ynot.traits;

import mcjty.xnet.api.helper.AbstractConnectorSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class TraitedConnectorSettings extends AbstractConnectorSettings {
	public static final ResourceLocation iconXnetGuiElements = new ResourceLocation("xnet", "textures/gui/guielements.png");

	private final List<Trait> traits = new ArrayList<>();

	public TraitedConnectorSettings(@Nonnull EnumFacing side) {
		super(side);
	}

	protected final List<Trait> getTraits() {
		return traits;
	}

	protected void register(Trait trait) {
		traits.add(trait);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		for (Trait t : getTraits()) {
			t.readFromNBT(tag);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		for (Trait t : getTraits()) {
			t.writeToNBT(tag);
		}
	}

	@Override
	public void update(Map<String, Object> data) {
		super.update(data);
		for (Trait t : getTraits()) {
			t.update(data);
		}
	}
}
