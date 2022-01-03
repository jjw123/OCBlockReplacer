package dev.jjw123.ocblockreplacer.client.renderer;

import com.google.common.primitives.SignedBytes;
import dev.jjw123.ocblockreplacer.tileentity.Replacer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ReplacerTESR extends TileEntitySpecialRenderer<Replacer> {

    @Override
    public void render(Replacer te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {

        float timeD = (float) (360D * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL) - partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);

        renderItem(/*new ItemStack(Blocks.DIRT, 64)*/te.getStackInSlot(0), 0.5f, 0.15f, 0.25f, 0.7f, partialTicks, timeD);

        for(int i = 1; i < te.getSizeInventory(); i++) {

            renderItem(/*new ItemStack(Blocks.SAND, 64)*/te.getStackInSlot(i), 0.88f - (i * 0.08f), 0.22f, 0.9f - (i % 2 * 0.2f), 0.4f, partialTicks, timeD);
        }

        GlStateManager.popMatrix();
    }

    public void renderItem(ItemStack item, float x, float y, float z, float scale, float partialTicks, float timeD) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(timeD*1.5f, 0F, 1F, 0F);
        GlStateManager.scale(scale, scale, scale);

        setLightmapDisabled(true);

        EntityItem customItem = new EntityItem(this.getWorld());
        customItem.setItem(item);
        customItem.hoverStart = 0F;

        RenderEntityItem itemRenderer = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem()) {
            @Override
            public int getModelCount(ItemStack stack) {

                return SignedBytes.saturatedCast(Math.min(stack.getCount() / 32, 15) + 1);
            }

            @Override
            public boolean shouldBob() {

                return false;
            }

            @Override
            public boolean shouldSpreadItems() {

                return true;
            }
        };

        itemRenderer.doRender(customItem, 0D, 0D, 0D, 0F, partialTicks);
        GlStateManager.popMatrix();
    }
}
