package pl.asie.ynot.oc;

import li.cil.oc.api.network.Environment;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.blocks.controller.gui.GuiController;
import net.minecraft.util.EnumFacing;
import pl.asie.ynot.YNot;
import pl.asie.ynot.enums.OCNetworkMode;
import pl.asie.ynot.traits.TraitEnum;
import pl.asie.ynot.traits.TraitedConnectorSettings;

import javax.annotation.Nullable;

public class OCConnectorSettings extends TraitedConnectorSettings {
    TraitEnum<OCNetworkMode> networkMode;

    OCConnectorSettings(EnumFacing side) {
        super(side);

        register(networkMode = new TraitEnum<>("mode", OCNetworkMode.class, OCNetworkMode.COMPONENT_AND_NETWORK));
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        switch (networkMode.get()) {
            case COMPONENT_AND_NETWORK:
                return new IndicatorIcon(YNot.iconGui, 26, 10, 13, 10);
            case NETWORK_ONLY:
            default:
                return new IndicatorIcon(YNot.iconGui, 13, 10, 13, 10);
        }
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
        networkMode.apply("Components + Network or Network only.", gui);
    }
}
