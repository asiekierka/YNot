package pl.asie.ynot.oc;

import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.SidedEnvironment;
import mcjty.xnet.api.channels.IChannelSettings;
import mcjty.xnet.api.channels.IChannelType;
import mcjty.xnet.api.channels.IConnectorSettings;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OCChannelType implements IChannelType {
    @Override
    public String getID() {
        return "ynot.opencomputers";
    }

    @Override
    public String getName() {
        return "OpenComputers";
    }

    @Override
    public boolean supportsBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        return world.getTileEntity(pos.offset(side)) instanceof Environment
                || world.getTileEntity(pos.offset(side)) instanceof SidedEnvironment;
    }

    @Nonnull
    @Override
    public IConnectorSettings createConnector(@Nonnull EnumFacing side) {
        return new OCConnectorSettings(side);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new OCChannelSettings();
    }
}
