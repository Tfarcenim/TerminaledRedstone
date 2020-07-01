package tfar.terminaledredstone;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class NBTUtil {

	public static CompoundNBT writeBlockPos(CompoundNBT nbt,@Nullable BlockPos pos) {
		if (pos == null)return nbt;
		CompoundNBT tag = new CompoundNBT();
		tag.putInt("x",pos.getX());
		tag.putInt("y",pos.getY());
		tag.putInt("z",pos.getZ());
		nbt.put("connected",tag);
		return nbt;
	}

	@Nullable
	public static BlockPos readBlockPos(CompoundNBT nbt) {
		if (!nbt.contains("connected"))return null;
		CompoundNBT tag = nbt.getCompound("connected");
		return new BlockPos(tag.getInt("x"),tag.getInt("y"),tag.getInt("z"));
	}

	public static CompoundNBT writeBlockPosList(CompoundNBT nbt, List<BlockPos> posList) {
		ListNBT listNBT = new ListNBT();
		posList.forEach(pos -> {
		CompoundNBT tag = new CompoundNBT();
		tag.putInt("x",pos.getX());
		tag.putInt("y",pos.getY());
		tag.putInt("z",pos.getZ());
		listNBT.add(tag);
	});
		nbt.put("connections",listNBT);
		return nbt;
	}

	public static void readBlockPosList(CompoundNBT nbt,List<BlockPos> posList) {
		posList.clear();
		ListNBT tag = nbt.getList("connections", Constants.NBT.TAG_COMPOUND);
		tag.stream().map(CompoundNBT.class::cast).forEach(nbt1 ->
						posList.add(new
										BlockPos(nbt1.getInt("x"),
										nbt1.getInt("y"),
										nbt1.getInt("z"))));
	}
}
