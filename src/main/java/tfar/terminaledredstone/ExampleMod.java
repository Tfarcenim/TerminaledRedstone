package tfar.terminaledredstone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    // Directly reference a log4j logger.

    public static final String MODID = "terminaledredstone";

    private static final Logger LOGGER = LogManager.getLogger();

    public static Block REDSTONE_TERMINAL;
    public static Item REDSTONE_SPOOL;
    public static TileEntityType<?> BLOCK_ENTITY;

    public ExampleMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        bus.addListener(this::setup);
        // Register the doClientStuff method for modloading
        bus.addListener(this::doClientStuff);
        bus.addGenericListener(Block.class,this::blocks);
        bus.addGenericListener(Item.class,this::items);
        bus.addGenericListener(TileEntityType.class,this::blockentities);
    }

    private void blocks(final RegistryEvent.Register<Block> event) {
        REDSTONE_TERMINAL = register(new RedstoneTerminalBlock(Block.Properties.from(Blocks.DAYLIGHT_DETECTOR)),"redstone_terminal",event.getRegistry());
    }


    private void items(final RegistryEvent.Register<Item> event) {
        REDSTONE_SPOOL = register(new RedstoneWireSpoolItem(new Item.Properties().group(ItemGroup.REDSTONE)),"redstone_spool",event.getRegistry());
        register(new BlockItem(REDSTONE_TERMINAL,new Item.Properties().group(ItemGroup.REDSTONE)),
                "redstone_terminal", event.getRegistry());
    }

    private void blockentities(final RegistryEvent.Register<TileEntityType<?>> event) {
        BLOCK_ENTITY = register(
                TileEntityType.Builder.create(RedstoneTerminalBlockEntity::new,REDSTONE_TERMINAL).build(null),
                "redstone_terminal",event.getRegistry());
    }



    private void setup(final FMLCommonSetupEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        EVENT_BUS.addListener(Client::render);
        RenderTypeLookup.setRenderLayer(REDSTONE_TERMINAL,RenderType.translucent());
    }

    private static <T extends IForgeRegistryEntry<T>> T register(T obj, String name, IForgeRegistry<T> registry) {
        registry.register(obj.setRegistryName(new ResourceLocation(MODID, name)));
        return obj;
    }
}
