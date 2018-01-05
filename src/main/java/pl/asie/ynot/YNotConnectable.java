package pl.asie.ynot;

import mcjty.xnet.api.IXNet;
import mcjty.xnet.api.channels.IConnectable;
import mcjty.xnet.apiimpl.XNetApi;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class YNotConnectable implements IConnectable {
	protected static final YNotConnectable INSTANCE = new YNotConnectable();
	private final List<Class> classes = new ArrayList<>();
	private final List<Capability> caps = new ArrayList<>();

	public static void add(Capability... c) {
		INSTANCE.caps.addAll(Arrays.asList(c));
	}

	public static void add(Class... c) {
		INSTANCE.classes.addAll(Arrays.asList(c));
	}

	@Override
	public ConnectResult canConnect(@Nonnull IBlockAccess access, @Nonnull BlockPos connectorPos, @Nonnull BlockPos blockPos, @Nullable TileEntity tile, @Nonnull EnumFacing facing) {
		if (tile != null) {
			for (Class c : classes) {
				if (c.isAssignableFrom(tile.getClass())) {
					return ConnectResult.YES;
				}
			}

			for (Capability c : caps) {
				if (tile.hasCapability(c, facing)) {
					return ConnectResult.YES;
				}
			}
		}

		return ConnectResult.DEFAULT;
	}
}
