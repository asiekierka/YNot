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

import com.reddit.user.koppeh.flamingo.BlockFlamingo;
import mcjty.xnet.api.channels.IChannelSettings;
import mcjty.xnet.api.channels.IChannelType;
import mcjty.xnet.api.channels.IConnectable;
import mcjty.xnet.api.channels.IConnectorSettings;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.ynot.mekanism.GasChannelSettings;
import pl.asie.ynot.mekanism.GasConnectorSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlamingoChannelType implements IChannelType, IConnectable {
	@Override
	public String getID() {
		return "ynot.flamingo";
	}

	@Override
	public String getName() {
		return "Wiggles (Flamingo)";
	}

	@Override
	public boolean supportsBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
		return world.getBlockState(pos).getBlock() instanceof BlockFlamingo;
	}

	@Nonnull
	@Override
	public IConnectorSettings createConnector(@Nonnull EnumFacing side) {
		return new FlamingoConnectorSettings(side);
	}

	@Nonnull
	@Override
	public IChannelSettings createChannel() {
		return new FlamingoChannelSettings();
	}

	@Override
	public ConnectResult canConnect(@Nonnull IBlockAccess access, @Nonnull BlockPos connectorPos, @Nonnull BlockPos blockPos, @Nonnull EnumFacing facting) {
		return ConnectResult.YES;
	}
}
