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
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Map;

public class TraitInteger extends Trait<Integer> {
	protected Integer val;
	private final Integer def;

	public TraitInteger(String tag, @Nullable Integer defaultValue) {
		super(tag);
		this.def = defaultValue;
	}

	@Override
	public void readFromNBT(NBTTagCompound cpt) {
		if (cpt.hasKey(tag, Constants.NBT.TAG_ANY_NUMERIC)) {
			val = cpt.getInteger(tag);
		} else {
			val = null;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound cpt) {
		if (val != null) {
			cpt.setInteger(tag, val);
		}
	}

	@Override
	public void update(Map<String, Object> data) {
		if (data.containsKey(tag)) {
			val = (Integer) data.get(tag);
		}
	}

	@Override
	public Integer get() {
		return val != null ? val : def;
	}

	@Override
	public IEditorGui apply(String tip, IEditorGui gui) {
		return gui.integer(tag, tip, val, 36);
	}
}
