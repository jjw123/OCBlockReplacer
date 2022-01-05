package dev.jjw123.ocblockreplacer;

import dev.jjw123.ocblockreplacer.block.BlockReplacer;
import dev.jjw123.ocblockreplacer.client.renderer.ReplacerTESR;
import dev.jjw123.ocblockreplacer.tileentity.Replacer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.HashSet;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

@Mod(
        modid = OCBlockReplacer.MOD_ID,
        name = OCBlockReplacer.MOD_NAME,
        version = "1.0.2",
        dependencies = "required-after:opencomputers"
)
public class OCBlockReplacer {

    public static final String MOD_ID = "ocblockreplacer";
    public static final String MOD_NAME = "OC Block Replacer";

    @Mod.Instance(MOD_ID)
    public static OCBlockReplacer INSTANCE;

    static HashSet<Block> modBlocks = new HashSet<>();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        modBlocks.add(new BlockReplacer());
    }

    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {

        @SubscribeEvent
        public static void onItemRegistry(RegistryEvent.Register<Item> event) {

            for(Block block : modBlocks)
                event.getRegistry().register(new ItemBlock(block).setRegistryName(MOD_ID, "replacer").setTranslationKey("replacer"));
        }

        @SubscribeEvent
        public static void onBlockRegistry(RegistryEvent.Register<Block> event) {

            for(Block block : modBlocks)
                event.getRegistry().register(block);

            GameRegistry.registerTileEntity(Replacer.class, new ResourceLocation(MOD_ID, "replacer"));

        }

    }

    @Mod.EventBusSubscriber(value = CLIENT)
    public static class ClientEventSubscriber {

        @SubscribeEvent
        public static void onModelRegistryEvent(@Nonnull final ModelRegistryEvent event) {

            ClientRegistry.bindTileEntitySpecialRenderer(Replacer.class, new ReplacerTESR());

            for(Block block : modBlocks)
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }

    }
}
