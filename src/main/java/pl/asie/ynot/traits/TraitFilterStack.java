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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class TraitFilterStack extends Trait<ItemStack> {
	private ItemStack filter = ItemStack.EMPTY;

	public TraitFilterStack(String tag) {
		super(tag);
	}

	@Override
	public void readFromNBT(NBTTagCompound cpt) {
		if (cpt.hasKey(tag, Constants.NBT.TAG_COMPOUND)) {
			filter = new ItemStack(cpt.getCompoundTag(tag));
		} else {
			filter = ItemStack.EMPTY;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound cpt) {
		if (!filter.isEmpty()) {
			NBTTagCompound item = new NBTTagCompound();
			filter.writeToNBT(item);
			cpt.setTag(tag, item);
		}
	}

	@Override
	public void update(Map<String, Object> data) {
		if (data.containsKey(tag)) {
			filter = (ItemStack) data.get(tag);
		}
		if (filter == null) {
			filter = ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack get() {
		return filter;
	}

	@Override
	public IEditorGui apply(String tip, IEditorGui gui) {
		return gui.ghostSlot(tag, filter);
	}
}
