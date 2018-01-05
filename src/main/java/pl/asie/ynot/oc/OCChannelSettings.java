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

package pl.asie.ynot.oc;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.*;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import mcjty.lib.varia.WorldTools;
import mcjty.xnet.api.channels.IConnectorSettings;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.keys.SidedConsumer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import pl.asie.ynot.YNot;
import pl.asie.ynot.enums.OCNetworkMode;
import pl.asie.ynot.traits.TraitedChannelSettings;

import javax.annotation.Nullable;
import java.util.*;

public class OCChannelSettings extends TraitedChannelSettings {
    public class NodeEntry {
        private final OCConnectorSettings settings;
        private final BlockPos pos;
        private final EnumFacing facing;
        private final OCNetworkMode mode;
        private TileEntity tile;
        private Node node;

        public NodeEntry(OCConnectorSettings settings, BlockPos pos, EnumFacing facing, OCNetworkMode mode) {
            this.settings = settings;
            this.pos = pos;
            this.facing = facing;
            this.mode = mode;
        }

        public void remove() {
            if (node != null) {
                if (mode.hasComponent()) {
                    node.disconnect(channelNode);
                }

                tile = null;
                node = null;
            }
        }

        public void update(World world, IControllerContext context, int ticker) {
            if (node != null) {
                if (!context.matchColor(settings.getColorsMask()) || (tile != null && tile.isInvalid())) {
                    remove();
                }
            }

            if (tile == null && ticker == 0) {
                if (WorldTools.chunkLoaded(world, pos) && context.matchColor(settings.getColorsMask())) {
                    TileEntity tile = world.getTileEntity(pos);
                    Node node = getNode(tile, facing);
                    if (node != null) {
                        this.tile = tile;
                        this.node = node;

                        if (mode.hasComponent()) {
                            if (!node.isNeighborOf(channelNode)) {
                                node.connect(channelNode);
                            }
                        }
                    }
                }
            }
        }
    }

    @CapabilityInject(Environment.class)
    private static Capability<Environment> ENVIRONMENT_CAPABILITY;

    @CapabilityInject(SidedEnvironment.class)
    private static Capability<SidedEnvironment> SIDED_ENVIRONMENT_CAPABILITY;

    Node channelNode;
    Map<SidedConsumer, NodeEntry> componentNodes;
    Map<SidedConsumer, NodeEntry> networkNodes;
    boolean shouldCleanCache = true;
    int ticker;

    class DummyEnvironment extends AbstractManagedEnvironment {
        DummyEnvironment() {
            this.setNode(Network.newNode(this, Visibility.Network).create());
        }

        @Override
        public void onMessage(Message message) {
            super.onMessage(message);

            if (message.name().equals("network.message")) {
                for (NodeEntry node : networkNodes.values()) {
                    if (node.node != null) {
                        node.node.sendToAddress("network.message", node.node.address(), message.data());
                    }
                }
            }
        }
    }

    public OCChannelSettings() {
        super();

        channelNode = new DummyEnvironment().node();
        componentNodes = new LinkedHashMap<>();
        networkNodes = new LinkedHashMap<>();
    }

    private Node getNode(TileEntity tile, EnumFacing side) {
        if (tile == null) {
            return null;
        } else if (tile.hasCapability(SIDED_ENVIRONMENT_CAPABILITY, side)) {
            return tile.getCapability(SIDED_ENVIRONMENT_CAPABILITY, side).sidedNode(side);
        } else if (tile.hasCapability(ENVIRONMENT_CAPABILITY, side)) {
            return tile.getCapability(ENVIRONMENT_CAPABILITY, side).node();
        } else {
            return null;
        }
    }

    private void removeNotPresent(Map<SidedConsumer, NodeEntry> entries, Map<SidedConsumer, IConnectorSettings> connectors, Map<SidedConsumer, IConnectorSettings> routedConnectors) {
        Iterator<SidedConsumer> it = entries.keySet().iterator();
        while (it.hasNext()) {
            SidedConsumer consumer = it.next();
            if (!connectors.containsKey(consumer) && !routedConnectors.containsKey(consumer)) {
                entries.get(consumer).remove();
                it.remove();
            }
        }
    }

    @Override
    public void tick(int channel, IControllerContext context) {
        if (shouldCleanCache) {
            Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
            Map<SidedConsumer, IConnectorSettings> routedConnectors = context.getRoutedConnectors(channel);

            removeNotPresent(componentNodes, connectors, routedConnectors);
            removeNotPresent(networkNodes, connectors, routedConnectors);

            World world = context.getControllerWorld();
            addNewConnectors(context, world, connectors);
            addNewConnectors(context, world, routedConnectors);
            ticker = 0;
            shouldCleanCache = false;
        }

        for (NodeEntry node : componentNodes.values()) {
            node.update(context.getControllerWorld(), context, ticker);
        }

        for (NodeEntry node : networkNodes.values()) {
            node.update(context.getControllerWorld(), context, ticker);
        }

        ticker = (ticker + 1) % 20;
    }

    private void addNewConnectors(IControllerContext context, World world, Map<SidedConsumer, IConnectorSettings> connectors) {
        for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
            OCConnectorSettings settings = (OCConnectorSettings) entry.getValue();
            NodeEntry oldEntry = componentNodes.get(entry.getKey());
            if (oldEntry == null) {
                oldEntry = networkNodes.get(entry.getKey());
            }

            if (oldEntry != null && (oldEntry.mode != settings.networkMode.get() || oldEntry.facing != settings.getFacing())) {
                oldEntry.remove();
                componentNodes.remove(entry.getKey());
                networkNodes.remove(entry.getKey());
                oldEntry = null;
            }

            if (oldEntry == null) {
                BlockPos pos = context.findConsumerPosition(entry.getKey().getConsumerId());
                pos = pos.offset(entry.getKey().getSide());
                NodeEntry nodeEntry = new NodeEntry(settings, pos, settings.getFacing(), settings.networkMode.get());

                if (nodeEntry.mode.hasComponent()) {
                    componentNodes.put(entry.getKey(), nodeEntry);
                } else {
                    networkNodes.put(entry.getKey(), nodeEntry);
                }
            }
        }
    }

    @Override
    public void cleanCache() {
        shouldCleanCache = true;
    }

    @Override
    public int getColors() {
        return 0;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return new IndicatorIcon(YNot.iconGui, 22, 0, 11, 10);
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
