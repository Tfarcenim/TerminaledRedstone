package tfar.terminaledredstone;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class BlockPosPair {

	public final BlockPos pos1;
	public final BlockPos pos2;

	public BlockPosPair(BlockPos pos1, BlockPos pos2) {
		this.pos1 = pos1;
		this.pos2 = pos2;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BlockPosPair))return false;
		BlockPosPair blockPosPair = (BlockPosPair)obj;
		return blockPosPair.pos1.equals(this.pos1) && blockPosPair.pos2.equals(this.pos2) ||
						blockPosPair.pos1.equals(this.pos2) && blockPosPair.pos2.equals(this.pos1);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pos1,pos2) + Objects.hash(pos2,pos1);
	}
}
