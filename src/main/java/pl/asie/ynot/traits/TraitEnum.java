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

public class TraitEnum<T extends Enum> extends Trait<T> {
	private final Class<T> tClass;
	private T mode, defaultValue;

	public TraitEnum(String tag, Class<T> tClass, T defaultValue) {
		super(tag);
		this.tClass = tClass;
		this.mode = defaultValue;
		this.defaultValue = defaultValue;
	}

	@Override
	public void readFromNBT(NBTTagCompound cpt) {
		byte b = cpt.getByte(tag);
		if (tClass.getEnumConstants().length > b) {
			mode = tClass.getEnumConstants()[b];
		} else {
			mode = defaultValue;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound cpt) {
		cpt.setByte(tag, (byte) mode.ordinal());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void update(Map<String, Object> data) {
		if (data.containsKey(tag)) {
			String s = ((String) data.get(tag)).toUpperCase();
			for (Enum e : tClass.getEnumConstants()) {
				if (s.equals(e.name())) {
					mode = (T) e;
				}
			}
		}
	}

	@Override
	public T get() {
		return mode;
	}

	@Override
	public IEditorGui apply(String tip, IEditorGui gui) {
		return gui.choices(tag, tip, mode, tClass.getEnumConstants());
	}

}
