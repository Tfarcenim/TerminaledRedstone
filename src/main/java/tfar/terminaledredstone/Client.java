package tfar.terminaledredstone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Client {

	public static void render(RenderWorldLastEvent e) {
		List<RedstoneTerminalBlockEntity> blockEntities = Minecraft.getInstance().world.loadedTileEntityList
						.stream()
						.filter(RedstoneTerminalBlockEntity.class::isInstance)
						.map(RedstoneTerminalBlockEntity.class::cast)
						.collect(Collectors.toList());

		Set<BlockPosPair> blockPosPairSet = new HashSet<>();

		for (RedstoneTerminalBlockEntity be : blockEntities) {

			for (BlockPos other : be.connectedTo) {

				boolean has = blockPosPairSet.contains(new BlockPosPair(be.getPos(),other));
				if (!has) {
					blockPosPairSet.add(new BlockPosPair(be.getPos(),other));
				}
			}
		}

		MatrixStack stack = e.getMatrixStack();

		IRenderTypeBuffer buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
				for (BlockPosPair pair : blockPosPairSet) {
					renderLeash(0, stack, buffer,pair.pos1,pair.pos2);
				}
		Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(RenderType.leash());
	}

	private static void renderLeash(float partialTicks, MatrixStack matrices, IRenderTypeBuffer bufferIn, BlockPos start, BlockPos end) {

			RedstoneTerminalBlockEntity otherTerminal = (RedstoneTerminalBlockEntity) Minecraft.getInstance().world.getTileEntity(end);

		if (otherTerminal != null) {
			matrices.push();
		Vec3d vec3d = TileEntityRendererDispatcher.instance.renderInfo.getProjectedView();
		matrices.translate(-vec3d.x, -vec3d.y, -vec3d.z);


		double xOffset1 = 0;//-dir1.getXOffset() / 8f;
		double yOffset1 = 0; //-dir1.getYOffset() / 8f;
		double zOffset1 = 0;// -dir1.getZOffset() / 8f;
		matrices.translate(start.getX() + .5 + xOffset1, start.getY() + .5 + yOffset1, start.getZ() + .5 + zOffset1);
			Direction dir2 = otherTerminal.getBlockState().get(RedstoneTerminalBlock.FACING);

			double xOffset2 = -dir2.getXOffset() / 8f;
			double yOffset2 = -dir2.getYOffset() / 8f;
			double zOffset2 = -dir2.getZOffset() / 8f;

			double x1 = start.getX() + .5 + xOffset1;
			double y1 = start.getY() + .5 + yOffset1;
			double z1 = start.getZ() + .5 + zOffset1;

			double x2 = end.getX() + .5 + +xOffset2;
			double y2 = end.getY() + .5 + +yOffset2;
			double z2 = end.getZ() + .5 + zOffset2;

			float xDiff = (float) (x2 - x1);
			float yDiff = (float) (y2 - y1);
			float zDiff = (float) (z2 - z1);
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.leash());
			Matrix4f matrix4f = matrices.getLast().getPositionMatrix();
			float width = MathHelper.fastInvSqrt(xDiff * xDiff + zDiff * zDiff) * 0.0125f;
			float f5 = zDiff * width;
			float f6 = xDiff * width;
			int startLight = 15;//startlight
			int endLight = 15;//endlight
			int k = Minecraft.getInstance().world.getLightFor(LightType.SKY, start);
			int l = Minecraft.getInstance().world.getLightFor(LightType.SKY, end);

			float f1 = .05f;
			int power = 0;//terminalBlockEntity.getWorld().getBlockState(terminalBlockEntity.getPos()).get(RedstoneTerminalBlock.POWER);

			renderSide(ivertexbuilder, matrix4f, xDiff, yDiff, zDiff, startLight, endLight, k, l, f1, f1, f5, f6, power);
			renderSide(ivertexbuilder, matrix4f, xDiff, yDiff, zDiff, startLight, endLight, k, l, f1, 0, f5, f6, power);
			matrices.pop();
		}
	}

	public static void renderSide(IVertexBuilder bufferIn, Matrix4f matrix, float x, float y, float z, int blockLight, int holderBlockLight, int skyLight, int holderSkyLight, float height, float widthY, float x2, float z2, int power) {

		for(int j = 0; j < 24; ++j) {
			float f = j / 23F;
			int k = (int)MathHelper.lerp(f, blockLight, holderBlockLight);
			int l = (int)MathHelper.lerp(f, skyLight, holderSkyLight);
			int i1 = LightTexture.packLight(k, l);
			addVertexPair(bufferIn, matrix, i1, x, y, z, height, widthY, 24, j, false, x2, z2,power);
			addVertexPair(bufferIn, matrix, i1, x, y, z, height, widthY, 24, j + 1, true, x2, z2,power);
		}

	}

	public static void addVertexPair(IVertexBuilder bufferIn, Matrix4f matrixIn, int packedLight, float x2, float y2, float z2, float height, float y1, int total, int index, boolean end, float x1, float z1, int power) {
		float red = 1;//.25f + .05f * power;
		float green = 0;
		float blue = 0;

		float delta = (float)index / total;
		float x = x2 * delta;
		float y = y2 * (delta * delta + delta) * .5f;
		float z = z2 * delta;
		if (!end) {
			bufferIn.pos(matrixIn, x + x1, y + height - y1, z - z1).color(red, green, blue, 1.0F)
							.lightmap(packedLight).endVertex();
		}

		bufferIn.pos(matrixIn, x - x1, y + y1, z + z1).color(red, green, blue, 1.0F).lightmap(packedLight).endVertex();
		if (end) {
			bufferIn.pos(matrixIn, x + x1, y + height - y1, z - z1).color(red, green, blue, 1.0F).lightmap(packedLight).endVertex();
		}
	}
}
