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
import pl.asie.ynot.traits.TraitedChannelSettings;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OCChannelSettings extends TraitedChannelSettings {
    Node channelNode;
    Set<Node> cachedNodes = null;

    class DummyEnvironment extends AbstractManagedEnvironment {
        DummyEnvironment() {
            this.setNode(Network.newNode(this, Visibility.Neighbors).create());
        }
    }

    public OCChannelSettings() {
        super();

        channelNode = new DummyEnvironment().node();
        cachedNodes = null;
    }

    @Override
    public void tick(int channel, IControllerContext context) {
        if(cachedNodes != null) { return; }
        cachedNodes = new HashSet<>();

        Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
        for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
            OCConnectorSettings settings = (OCConnectorSettings) entry.getValue();
            Environment env = (Environment)
                    context.getControllerWorld()
                        .getTileEntity(context.findConsumerPosition(entry.getKey().getConsumerId())
                            .add(entry.getKey().getSide().getDirectionVec()));

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
        return null;
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    @Override
    public boolean isEnabled(String tag) {
        return false;
    }

    @Override
    public void createGui(IEditorGui gui) {

    }
}
