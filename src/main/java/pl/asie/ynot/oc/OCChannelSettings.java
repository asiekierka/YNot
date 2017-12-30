package pl.asie.ynot.oc;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import mcjty.xnet.api.channels.IChannelSettings;
import mcjty.xnet.api.channels.IConnectorSettings;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.keys.SidedConsumer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.ynot.YNot;
import pl.asie.ynot.traits.TraitedChannelSettings;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OCChannelSettings extends TraitedChannelSettings {
    Node channelNode;
    Set<Node> cachedNodes;

    class DummyEnvironment extends AbstractManagedEnvironment {
        DummyEnvironment() {
            this.setNode(Network.newNode(this, Visibility.Network).create());
        }
    }

    public OCChannelSettings() {
        super();

        channelNode = new DummyEnvironment().node();
        cachedNodes = null;
    }

    private Environment getEnvironment(World world, BlockPos pos) {
        if(world.getTileEntity(pos) instanceof Environment) {
            return (Environment) world.getTileEntity(pos);
        } else {
            return null;
        }
    }

    @Override
    public void tick(int channel, IControllerContext context) {
        if(cachedNodes != null) { return; }
        cachedNodes = new HashSet<>();

        World world = context.getControllerWorld();
        Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
        for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
            OCConnectorSettings settings = (OCConnectorSettings) entry.getValue();

            BlockPos pos = context.findConsumerPosition(entry.getKey().getConsumerId());
            pos = pos.offset(entry.getKey().getSide());

            Environment env = getEnvironment(world, pos);

            if(env == null) { continue; }
            if(env.node() != null && !env.node().isNeighborOf(channelNode)) {
                env.node().connect(channelNode);
                cachedNodes.add(env.node());
            }
        }

        connectors = context.getRoutedConnectors(channel);
        for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
            OCConnectorSettings settings = (OCConnectorSettings) entry.getValue();

            BlockPos pos = context.findConsumerPosition(entry.getKey().getConsumerId());
            pos = pos.offset(entry.getKey().getSide());

            Environment env = getEnvironment(world, pos);

            if(env == null) { continue; }
            if(env.node() != null && !env.node().isNeighborOf(channelNode)) {
                env.node().connect(channelNode);
                cachedNodes.add(env.node());
            }
        }

    }

    @Override
    public void cleanCache() {
        if(cachedNodes == null) { return; }

        for(Node node : cachedNodes) {
            node.disconnect(channelNode);
        }

        cachedNodes = null;
    }

    @Override
    public int getColors() {
        return 0;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return new IndicatorIcon(YNot.iconGui, 11, 0, 11, 10);
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    @Override
    public boolean isEnabled(String tag) {
        return true;
    }

    @Override
    public void createGui(IEditorGui gui) {

    }
}
