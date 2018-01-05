package pl.asie.ynot.oc;

import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.SidedEnvironment;
import mcjty.xnet.api.channels.IChannelSettings;
import mcjty.xnet.api.channels.IChannelType;
import mcjty.xnet.api.channels.IConnectorSettings;
import mekanism.api.gas.IGasHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OCChannelType implements IChannelType {
    @CapabilityInject(Environment.class)
    private static Capability<Environment> ENVIRONMENT_CAPABILITY;

    @CapabilityInject(SidedEnvironment.class)
    private static Capability<SidedEnvironment> SIDED_ENVIRONMENT_CAPABILITY;

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
        TileEntity tile = world.getTileEntity(pos.offset(side));

        if (tile != null) {
            if(tile.hasCapability(SIDED_ENVIRONMENT_CAPABILITY, side)) {
                return true;
            } else if(tile.hasCapability(ENVIRONMENT_CAPABILITY, side)) {
                return true;
            }
        }

        return false;
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
