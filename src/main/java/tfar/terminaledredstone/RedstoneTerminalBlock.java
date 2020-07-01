package tfar.terminaledredstone;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

public class RedstoneTerminalBlock extends Block {

	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final BooleanProperty INPUT = BooleanProperty.create("input");

	public static final VoxelShape DOWN = Block.makeCuboidShape(5, 9, 5, 11, 16, 11);
	public static final VoxelShape UP = Block.makeCuboidShape(5, 0, 5, 11, 7, 11);

	public static final VoxelShape NORTH = Block.makeCuboidShape(5, 5, 9, 11, 11, 16);
	public static final VoxelShape SOUTH = Block.makeCuboidShape(5, 5, 0, 11, 11, 7);

	public static final VoxelShape EAST = Block.makeCuboidShape(0, 5, 5, 7, 11, 11);
	public static final VoxelShape WEST = Block.makeCuboidShape(9, 5, 5, 16, 11, 11);


	public RedstoneTerminalBlock(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(INPUT, true));
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_) {
		ItemStack stack = player.getHeldItem(handIn);
		if (!worldIn.isRemote) {
			if (stack.getItem() == ExampleMod.REDSTONE_SPOOL) {
				if (stack.getOrCreateTag().contains("connected")) {
					BlockPos other = NBTUtil.readBlockPos(stack.getTag());
					if (other.equals(pos)) {
						player.sendMessage(new StringTextComponent("cannot connect a terminal to itself"));
					} else {
						BlockState otherState = worldIn.getBlockState(other);
						if (state.get(INPUT) && otherState.get(INPUT)) {
							player.sendMessage(new StringTextComponent("cannot connect 2 inputs together"));
						} else {
							RedstoneTerminalBlockEntity thisTerminal = (RedstoneTerminalBlockEntity) worldIn.getTileEntity(pos);
							thisTerminal.connect(other);

							RedstoneTerminalBlockEntity otherTerminal = (RedstoneTerminalBlockEntity) worldIn.getTileEntity(other);
							otherTerminal.connect(pos);
							worldIn.getPendingBlockTicks().scheduleTick(pos, this, 5);
							worldIn.getPendingBlockTicks().scheduleTick(other, this, 5);
							stack.getTag().remove("connected");
						}
					}
				} else {
					NBTUtil.writeBlockPos(stack.getOrCreateTag(), pos);
				}
			} else if (player.getHeldItem(handIn).isEmpty()) {
				worldIn.setBlockState(pos, state.with(INPUT, !state.get(INPUT)));
				player.sendMessage(new StringTextComponent("input: " + !state.get(INPUT)));
			}
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
		RedstoneTerminalBlockEntity redstoneTerminal = (RedstoneTerminalBlockEntity) world.getTileEntity(pos);

		if (state.get(INPUT)) {
			int power = world.getStrongPower(pos.offset(state.get(FACING).getOpposite()));
			for (BlockPos otherPos : redstoneTerminal.connectedTo) {
				BlockState otherState = world.getBlockState(otherPos);
				world.setBlockState(otherPos,otherState.with(POWER,power));
				world.getPendingBlockTicks().scheduleTick(otherPos, this, 5);
			}
		} else {
			int power = state.get(POWER);
			for (BlockPos otherPos : redstoneTerminal.connectedTo) {
				BlockState otherState = world.getBlockState(otherPos);
				if (!otherState.get(INPUT) && otherState.get(POWER) != power) {
					world.setBlockState(otherPos, otherState.with(POWER, power));
					world.getPendingBlockTicks().scheduleTick(otherPos, this, 5);
				}
			}
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		RedstoneTerminalBlockEntity be = (RedstoneTerminalBlockEntity) worldIn.getTileEntity(pos);
		if (newState.getBlock() != state.getBlock()) {
			if (!be.connectedTo.isEmpty()) {
				for (BlockPos otherPos : be.connectedTo) {
					RedstoneTerminalBlockEntity other = (RedstoneTerminalBlockEntity) worldIn.getTileEntity(otherPos);
					other.disconnect(pos);
				}
			}
		}
		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (state.get(INPUT))
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 5);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.get(FACING)) {
			case DOWN:
				return DOWN;
			case UP:
				return UP;

			case NORTH:
				return NORTH;
			case SOUTH:
				return SOUTH;

			case EAST:
				return EAST;
			case WEST:
				return WEST;
		}
		return DOWN;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(FACING, context.getFace());
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(POWER, FACING, INPUT);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
		return true;
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return !state.get(INPUT);
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return getWeakPower(blockState, blockAccess, pos, side);
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.get(INPUT) ? 0 : blockState.get(POWER);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new RedstoneTerminalBlockEntity();
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		RedstoneTerminalBlockEntity redstoneTerminalBlockEntity = (RedstoneTerminalBlockEntity) worldIn.getTileEntity(pos);
	}
}
