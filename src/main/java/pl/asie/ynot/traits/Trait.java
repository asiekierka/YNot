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

import mcjty.xnet.api.gui.IEditorGui;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public abstract class Trait<T> {
	protected final String tag;

	public Trait(String tag) {
		this.tag = tag;
	}

	public abstract void readFromNBT(NBTTagCompound cpt);
	public abstract void writeToNBT(NBTTagCompound cpt);
	public abstract void update(Map<String, Object> data);
	public abstract T get();
	public abstract IEditorGui apply(String tip, IEditorGui gui);

	public final String getTag() {
		return tag;
	}
}
