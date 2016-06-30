package zornco.bedcraftbeyond.beds.wooden;

import com.google.common.collect.Range;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import zornco.bedcraftbeyond.core.BedCraftBeyond;
import zornco.bedcraftbeyond.core.BcbBlocks;
import zornco.bedcraftbeyond.beds.base.BlockBedBase;
import zornco.bedcraftbeyond.beds.base.ItemFrame;
import zornco.bedcraftbeyond.core.util.SettingsHelper;
import zornco.bedcraftbeyond.beds.frames.registry.FrameException;
import zornco.bedcraftbeyond.beds.frames.registry.FrameHelper;
import zornco.bedcraftbeyond.beds.frames.registry.FrameRegistry;
import zornco.bedcraftbeyond.beds.frames.registry.FrameWhitelist;

import java.util.List;

public class ItemWoodenFrame extends ItemFrame {

    public ItemWoodenFrame(Block b) {
        super(b);
        setUnlocalizedName("frames.wooden");
        this.setHasSubtypes(true);

        GameRegistry.register(this);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    @SideOnly(Side.CLIENT)
    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubItems(Item item, CreativeTabs tab, List subItems) {
        // TODO: Fix list of wooden beds.
        FrameWhitelist wood =  FrameRegistry.getFrameWhitelist(FrameRegistry.EnumFrameType.WOOD);
        for(ResourceLocation rl : wood.getValidRegistryEntries()){

            try {
                for(Range<Integer> r : wood.getEntry(rl).getValidMeta().asRanges()){
                    if(r.hasLowerBound() && r.hasUpperBound()){
                        if(r.lowerEndpoint() == r.upperEndpoint()){
                            ItemStack stack = new ItemStack(this, 1, 0);
                            stack.setTagCompound(new NBTTagCompound());

                            // Set up frame
                            NBTTagCompound frameTag = new NBTTagCompound();
                            frameTag.setString("frameType", rl.toString());
                            frameTag.setInteger("frameMeta", r.lowerEndpoint());
                            stack.getTagCompound().setTag("frame", frameTag);

                            subItems.add(stack);
                            continue;
                        }

                        for(int curEntry = r.lowerEndpoint(); curEntry < r.upperEndpoint(); ++curEntry){

                            ItemStack stack = new ItemStack(this, 1, 0);
                            stack.setTagCompound(new NBTTagCompound());

                            // Set up frame
                            NBTTagCompound frameTag = new NBTTagCompound();
                            frameTag.setString("frameType", rl.toString());
                            frameTag.setInteger("frameMeta", curEntry);
                            stack.getTagCompound().setTag("frame", frameTag);

                            subItems.add(stack);
                        }

                    }
                }


            } catch (FrameException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings({"deprecation"})
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tags, boolean advanced) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("frame")){
            tags.add(TextFormatting.RED + I18n.format(BedCraftBeyond.MOD_ID + ".frames.errors.invalid_frame"));
            return;
        }


        NBTTagCompound nbt = stack.getTagCompound();
        NBTTagCompound frameTag = nbt.getCompoundTag("frame");
        ItemStack frameStack = FrameHelper.getItemFromFrameTag(frameTag);
        if(frameStack != null) tags.add(TextFormatting.GREEN + "Frame: " + TextFormatting.RESET + frameStack.getDisplayName());

        if(SettingsHelper.showingAdvancedTooltips()) {
            tags.add(TextFormatting.AQUA + frameTag.getString("frameType") + ":" + frameTag.getInteger("frameMeta"));
            tags.add("");
        }
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (side != EnumFacing.UP) return EnumActionResult.FAIL;

        // If frame not set, abort early- we need that.
        if(!stack.hasTagCompound() || !stack.getTagCompound().hasKey("frame")) return EnumActionResult.FAIL;

        boolean canPlaceBedHere = testSimpleBedPlacement(world, player, pos, stack);
        if (!canPlaceBedHere) return EnumActionResult.FAIL;

        pos = pos.up();
        try {
            BlockPos btmHalf = pos;
            BlockPos topHalf = btmHalf.offset(player.getHorizontalFacing());

            IBlockState foot = BcbBlocks.woodenBed.getDefaultState().withProperty(BlockBedBase.FACING, player.getHorizontalFacing())
                .withProperty(BlockBedBase.HEAD, false);
            if (!placeBedBlock(stack, world, player, btmHalf, foot, true))
                throw new Exception();

            IBlockState head = BcbBlocks.woodenBed.getDefaultState()
                .withProperty(BlockBedBase.HEAD, true)
                .withProperty(BlockBedBase.FACING, player.getHorizontalFacing().getOpposite());
            if (!placeBedBlock(stack, world, player, topHalf, head, true))
                throw new Exception("Failed to set blockstate.");
        } catch (Exception e) {
            return EnumActionResult.FAIL;
        }

        // If not creative mode, remove placer item
        if (!player.capabilities.isCreativeMode) new PlayerInvWrapper(player.inventory).extractItem(player.inventory.currentItem, 1, false);

        return EnumActionResult.SUCCESS;
    }
}
