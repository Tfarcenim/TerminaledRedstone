package tfar.terminaledredstone;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class RedstoneTerminalBlockEntity extends TileEntity {
	public RedstoneTerminalBlockEntity() {
		super(ExampleMod.BLOCK_ENTITY);
	}

	public final List<BlockPos> connectedTo = new ArrayList<>();

	public void connect(BlockPos other) {
		if (!connectedTo.contains(other)) {
			connectedTo.add(other);
			markDirty();
		}
	}

	public void disconnect(BlockPos other) {
		connectedTo.remove(other);
			markDirty();
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		NBTUtil.readBlockPosList(compound,connectedTo);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		world.notifyBlockUpdate(pos,getBlockState(),getBlockState(),3);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		NBTUtil.writeBlockPosList(compound,connectedTo);
		return super.write(compound);
	}

	@Nonnull
	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT nbt = write(new CompoundNBT());
		return nbt;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getPos(), 1, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
		this.read(packet.getNbtCompound());
	}
}
