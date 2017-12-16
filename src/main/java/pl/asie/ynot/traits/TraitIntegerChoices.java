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

public class TraitIntegerChoices extends TraitInteger {
	private String[] choiceStrs;
	private int[] choices;

	public TraitIntegerChoices(String tag, int[] choices) {
		super(tag, choices[0]);
		setChoices(choices);
	}

	public void setChoices(int[] c) {
		choices = c;
		choiceStrs = new String[c.length];
		for (int i = 0; i < c.length; i++) {
			choiceStrs[i] = Integer.toString(c[i]);
		}
		validate();
	}

	protected void validate() {
		if (val != null) {
			for (int i = 0; i < choices.length; i++) {
				if (val == choices[i]) {
					return;
				}
			}
		}
		val = choices[0];
	}

	@Override
	public void readFromNBT(NBTTagCompound cpt) {
		super.readFromNBT(cpt);
		validate();
	}

	@Override
	public void update(Map<String, Object> data) {
		if (data.containsKey(tag)) {
			val = Integer.parseInt((String) data.get(tag));
		}
		validate();
	}

	@Override
	public IEditorGui apply(String tip, IEditorGui gui) {
		validate();
		return gui.choices(tag, tip, Integer.toString(val), choiceStrs);
	}
}
