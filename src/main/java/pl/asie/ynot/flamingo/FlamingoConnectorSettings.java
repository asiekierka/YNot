package pl.asie.ynot.flamingo;

import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.blocks.controller.gui.GuiController;
import net.minecraft.util.EnumFacing;
import pl.asie.ynot.YNot;
import pl.asie.ynot.enums.InsertionMode;
import pl.asie.ynot.traits.TraitEnum;
import pl.asie.ynot.traits.TraitedConnectorSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlamingoConnectorSettings extends TraitedConnectorSettings {
	protected TraitEnum<InsertionMode> insertionMode;

	public FlamingoConnectorSettings(@Nonnull EnumFacing side) {
		super(side);
		register(insertionMode = new TraitEnum<>("mode", InsertionMode.class, InsertionMode.INS));
	}

	@Nullable
	@Override
	public IndicatorIcon getIndicatorIcon() {
		switch (insertionMode.get()) {
			case INS:
				return new IndicatorIcon(GuiController.iconGuiElements, 0, 70, 13, 10);
			case EXT:
			default:
				return new IndicatorIcon(GuiController.iconGuiElements, 13, 70, 13, 10);
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
		colorsGui(gui);
		redstoneGui(gui);
		gui.nl();
		insertionMode.apply("Insert or extract mode", gui);
	}
}
