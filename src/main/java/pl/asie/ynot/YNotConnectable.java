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
import java.util.*;

public class YNotConnectable implements IConnectable {
	private static final YNotConnectable INSTANCE = new YNotConnectable();
	private final List<Class> classes = new ArrayList<>();
	private final List<Capability> caps = new ArrayList<>();
	private final Set<Block> registeredBlocks = Collections.newSetFromMap(new IdentityHashMap<>());

	public static void add(Capability... c) {
		INSTANCE.caps.addAll(Arrays.asList(c));
	}

	public static void add(Class... c) {
		INSTANCE.classes.addAll(Arrays.asList(c));
	}

	public static void register(IXNet ixNet) {
		for (Block block : Block.REGISTRY) {
			int validStates = 0;

			for (IBlockState state : block.getBlockState().getValidStates()) {
				validStates |= (1 << block.getMetaFromState(state));
			}

			for (int i = 0; i < 16 && validStates > 0; i++, validStates >>= 1) {
				if ((validStates & 1) != 0) {
					if (block.hasTileEntity(block.getStateFromMeta(i))) {
						ixNet.registerConnectable(block.getRegistryName(), INSTANCE);
					}
				}
			}
		}
	}

	@Override
	public ConnectResult canConnect(@Nonnull IBlockAccess access, @Nonnull BlockPos connectorPos, @Nonnull BlockPos blockPos, @Nonnull EnumFacing facting) {
		TileEntity tile = access.getTileEntity(blockPos);

		for (Class c : classes) {
			if (c.isAssignableFrom(tile.getClass())) {
				return ConnectResult.YES;
			}
		}

		for (Capability c : caps) {
			if (tile.hasCapability(c, facting)) {
				return ConnectResult.YES;
			}
		}

		return ConnectResult.DEFAULT;
	}
}
