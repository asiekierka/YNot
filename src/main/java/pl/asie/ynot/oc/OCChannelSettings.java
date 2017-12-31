package pl.asie.ynot.oc;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.*;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import mcjty.xnet.api.channels.IConnectorSettings;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.keys.SidedConsumer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.ynot.YNot;
import pl.asie.ynot.enums.OCNetworkMode;
import pl.asie.ynot.traits.TraitedChannelSettings;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OCChannelSettings extends TraitedChannelSettings {
    Node channelNode;
    Set<Node> componentNodes;
    Set<Node> networkNodes;

    class DummyEnvironment extends AbstractManagedEnvironment {
        DummyEnvironment() {
            this.setNode(Network.newNode(this, Visibility.Network).create());
        }

        @Override
        public void onMessage(Message message) {
            super.onMessage(message);

            if(networkNodes == null) { return; }

            if(message.name().equals("network.message")) {
                for(Node node : networkNodes) {
                    node.sendToAddress("network.message", node.address(), message.data());
                }
            }
        }
    }

    public OCChannelSettings() {
        super();

        channelNode = new DummyEnvironment().node();
        componentNodes = null;
    }

    private Node getNode(World world, BlockPos pos, EnumFacing side) {
        if (world.getTileEntity(pos) instanceof SidedEnvironment) {
            return ((SidedEnvironment) world.getTileEntity(pos)).sidedNode(side.getOpposite());
        } else if(world.getTileEntity(pos) instanceof Environment) {
            return ((Environment) world.getTileEntity(pos)).node();
        } else {
            return null;
        }
    }

    @Override
    public void tick(int channel, IControllerContext context) {
        if(componentNodes != null) { return; }
        componentNodes = new HashSet<>();
        networkNodes = new HashSet<>();

        World world = context.getControllerWorld();
        processConnectors(context, world, context.getConnectors(channel));
        processConnectors(context, world, context.getRoutedConnectors(channel));
    }

    private void processConnectors(IControllerContext context, World world, Map<SidedConsumer, IConnectorSettings> connectors) {
        for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
            OCConnectorSettings settings = (OCConnectorSettings) entry.getValue();

            EnumFacing side = entry.getKey().getSide();
            BlockPos pos = context.findConsumerPosition(entry.getKey().getConsumerId());
            pos = pos.offset(side);

            Node node = getNode(world, pos, side);

            if(node == null) { continue; }
            if(settings.networkMode.get() == OCNetworkMode.COMPONENT_AND_NETWORK) {
                if (!node.isNeighborOf(channelNode)) {
                    node.connect(channelNode);
                    componentNodes.add(node);
                }
            } else {
                networkNodes.add(node);
            }
        }
    }

    @Override
    public void cleanCache() {
        if(componentNodes == null) { return; }

        for(Node node : componentNodes) {
            node.disconnect(channelNode);
        }

        networkNodes = null;
        componentNodes = null;
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
