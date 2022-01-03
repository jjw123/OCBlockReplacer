package dev.jjw123.ocblockreplacer.tileentity;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

import javax.annotation.Nullable;

public class Replacer extends TileEntityEnvironment implements ISidedInventory {

    private static final String TAG_NODE = "replacer";

    private NonNullList<ItemStack> replacerItemStacks = NonNullList.<ItemStack>withSize(10, ItemStack.EMPTY);

    public Replacer() {

        this.node = Network.newNode(this, Visibility.Network)
                .withComponent(TAG_NODE, Visibility.Network)
                .withConnector().create();
    }

    @Callback(doc = "function(posX, posZ, negX, negZ, y, block):boolean --  Replaces block of name 'block' with the block stored in the internal inventory")
    public Object[] replace(final Context context, final Arguments args) throws Exception {

        int posX = args.checkInteger(0);
        int posZ = args.checkInteger(1);
        int negX = args.checkInteger(2);
        int negZ = args.checkInteger(3);
        int y = args.checkInteger(4);
        String block = args.checkString(5);

        for(int x = this.getPos().getX() - negX; x <= this.getPos().getX() + posX; x++) {

            for(int z = this.getPos().getZ() - negZ; z <= this.getPos().getZ() + posZ; z++) {

                BlockPos pos = new BlockPos(x, this.getPos().getY() + y, z);

                IBlockState bs = this.world.getBlockState(pos);
                Block b = bs.getBlock();

                if(b.getRegistryName().toString().equals(block)) {

                    NonNullList<ItemStack> drops = NonNullList.create();
                    b.getDrops(drops, this.world, pos, bs, 0);

                    for(ItemStack drop : drops) {
                        for(int i = 1; i < replacerItemStacks.size(); i++) {

                            ItemStack stack = replacerItemStacks.get(i);

                            if(stack.isEmpty() || stack.getItem().getRegistryName().toString().equals(drop.getItem().getRegistryName().toString())) {

                                ItemStack s = drop.copy();
                                int count = stack.getCount() + drop.getCount();

                                if(count > getInventoryStackLimit()) {

                                    s.setCount(getInventoryStackLimit());
                                    drop.setCount(count - getInventoryStackLimit());
                                    replacerItemStacks.set(i, s);
                                } else {
                                    s.setCount(count);
                                    drop.setCount(0);
                                    replacerItemStacks.set(i, s);
                                    break;
                                }
                            }
                        }

                        if(!drop.isEmpty()) {

                            InventoryHelper.spawnItemStack(this.world, this.getPos().getX() + 0.5f, this.getPos().getY() + 1f, this.getPos().getZ() + 0.5f, drop);
                        }
                    }

                    ItemStack place = decrStackSize(0, 1);

                    if(!place.isEmpty() && place.getItem() instanceof ItemBlock && this.world instanceof WorldServer) {

                        ItemBlock ib = (ItemBlock)place.getItem();

                        IBlockState placeState = ib.getBlock().getStateForPlacement(this.world, pos, EnumFacing.UP, 0, 0, 0, place.getMetadata(), FakePlayerFactory.getMinecraft((WorldServer) this.world), EnumHand.MAIN_HAND);

                        world.setBlockState(pos, placeState);
                    }

                }
            }
        }
        markDirty();

        return null;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {

        if(side == EnumFacing.DOWN || side == EnumFacing.UP) {

            return new int[] {1,2,3,4,5,6,7,8,9};
        } else {
            return new int[] {0};
        }
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {

        if(index > 0) {

            return false;
        } else {

            return itemStackIn.getItem() instanceof ItemBlock;
        }
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    @Override
    public int getSizeInventory() {
        return replacerItemStacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.replacerItemStacks) {

            if (!itemstack.isEmpty()) {

                return false;
            }
        }

        return true;
    }

    public ItemStack getStackInSlot(int index)
    {
        return this.replacerItemStacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.replacerItemStacks, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.replacerItemStacks, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {

        this.replacerItemStacks.set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if(index > 0) {

            return true;
        } else {

            return stack.getItem() instanceof ItemBlock;
        }
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {}

    @Override
    public String getName() {
        return "replacer";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        this.replacerItemStacks = NonNullList.<ItemStack> withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.replacerItemStacks);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        ItemStackHelper.saveAllItems(compound, this.replacerItemStacks);

        return compound;
    }

    @Override
    public void markDirty() {

        this.world.markBlockRangeForRenderUpdate(pos, pos);
        this.world.notifyBlockUpdate(pos, getState(), getState(), 3);
        this.world.scheduleBlockUpdate(pos,this.getBlockType(),0,0);
    }

    private IBlockState getState() {

        return this.world.getBlockState(pos);
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getNbtCompound());
    }
}
