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

import mcjty.lib.varia.WorldTools;
import mcjty.xnet.api.channels.IChannelSettings;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.helper.AbstractConnectorSettings;
import mcjty.xnet.api.helper.DefaultChannelSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.ynot.mekanism.GasConnectorSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class TraitedChannelSettings extends DefaultChannelSettings implements IChannelSettings {
	private final List<Trait> traits = new ArrayList<>();

	protected final List<Trait> getTraits() {
		return traits;
	}

	protected void register(Trait trait) {
		traits.add(trait);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		for (Trait t : getTraits()) {
			t.readFromNBT(tag);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		for (Trait t : getTraits()) {
			t.writeToNBT(tag);
		}
	}

	@Override
	public void update(Map<String, Object> data) {
		for (Trait t : getTraits()) {
			t.update(data);
		}
	}

	protected boolean shouldCheck(IControllerContext context, BlockPos pos, AbstractConnectorSettings settings) {
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
}
