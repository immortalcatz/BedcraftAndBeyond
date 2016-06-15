package zornco.bedcraftbeyond.common.blocks;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.registry.GameRegistry;
import zornco.bedcraftbeyond.BedCraftBeyond;
import zornco.bedcraftbeyond.common.blocks.properties.EnumBedFabricType;
import zornco.bedcraftbeyond.common.blocks.properties.EnumBedPartStatus;
import zornco.bedcraftbeyond.common.blocks.properties.PropertyString;
import zornco.bedcraftbeyond.common.blocks.tiles.TileWoodenBed;
import zornco.bedcraftbeyond.common.item.BcbItems;
import zornco.bedcraftbeyond.common.item.linens.ItemBlanket;
import zornco.bedcraftbeyond.common.item.linens.ItemSheets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/***
 * A colored bed has sheets and blankets that are separately dyeable.
 * It works the same as a regular bed otherwise.
 */
public class BlockWoodenBed extends BlockBedBase implements IBedTileHolder {

    public static PropertyBool HAS_STORAGE = PropertyBool.create("storage");
    public static PropertyEnum<EnumBedPartStatus> STATUS = PropertyEnum.create("status", EnumBedPartStatus.class);

    public static PropertyEnum<EnumBedFabricType> BLANKETS = PropertyEnum.create("color_blankets", EnumBedFabricType.class);
    public static PropertyEnum<EnumBedFabricType> SHEETS = PropertyEnum.create("color_sheets", EnumBedFabricType.class);



    public enum EnumColoredPart {BLANKETS, SHEETS, PLANKS}

    public BlockWoodenBed() {
        setRegistryName(BedCraftBeyond.MOD_ID, "wooden_bed");
        setUnlocalizedName(BedCraftBeyond.MOD_ID + ".beds.wooden");
        setDefaultState(getDefaultState()
            .withProperty(FACING, EnumFacing.NORTH)
            .withProperty(OCCUPIED, false)
            .withProperty(HEAD, false)
            .withProperty(HAS_STORAGE, false)
            .withProperty(STATUS, EnumBedPartStatus.FOOT)
            .withProperty(BLANKETS, EnumBedFabricType.NONE)
            .withProperty(SHEETS, EnumBedFabricType.NONE));

        GameRegistry.register(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HEAD, OCCUPIED, FACING, HAS_STORAGE, STATUS, BLANKETS, SHEETS);
    }

    private boolean hasBlanketsAndSheets(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileWoodenBed tile = (TileWoodenBed) getTileForBed(world, state, pos);
        return tile.getLinenPart(EnumColoredPart.SHEETS, false) != null &&
            tile.getLinenPart(EnumColoredPart.BLANKETS, false) != null;
    }

    @Override
    public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, Entity player) {
        return hasBlanketsAndSheets(state, world, pos);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return state.getValue(HEAD) ? new TileWoodenBed(world) : null;
    }

    @Override
    public TileEntity getTileForBed(IBlockAccess world, IBlockState state, BlockPos pos) {
        if(state.getValue(HEAD))
            return world.getTileEntity(pos);

        if (!(state.getBlock() instanceof BlockWoodenBed)) return null;
        BlockPos actualTileHolder = pos.offset(state.getValue(FACING));

        TileEntity realHolder = world.getTileEntity(actualTileHolder);
        if (realHolder == null || !(realHolder instanceof TileWoodenBed)) return null;
        return realHolder;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileWoodenBed tile = (TileWoodenBed) getTileForBed(world, state, pos);

        if (tile == null) return true;
        if (world.isRemote) return true;

        state = getActualState(state, world, pos);
        // Add/remove blankets and sheets

        if (heldItem != null) {
            if (heldItem.getItem() instanceof ItemBlanket) {
                boolean set = tile.setLinenPart(EnumColoredPart.BLANKETS, heldItem);
                if (set) {
                    --heldItem.stackSize;
                    if (heldItem.stackSize < 1) player.setHeldItem(hand, null);
                }
            }

            if (heldItem.getItem() instanceof ItemSheets) {
                boolean set = tile.setLinenPart(EnumColoredPart.SHEETS, heldItem);
                if (set) {
                    --heldItem.stackSize;
                    if (heldItem.stackSize < 1) player.setHeldItem(hand, null);
                }
            }
        }

        if (heldItem == null) {
            if (player.isSneaking()) {
                // TODO: Open bed gui here
                ITextComponent message = new TextComponentString("");

                if (tile.getPartType(EnumColoredPart.BLANKETS) != EnumBedFabricType.NONE) {
                    message = new TextComponentString(TextFormatting.AQUA + "Blankets: " + TextFormatting.WHITE + tile.getPartColor(EnumColoredPart.BLANKETS));
                    player.addChatMessage(message);
                }

                if (tile.getPartType(EnumColoredPart.SHEETS) != EnumBedFabricType.NONE) {
                    message = new TextComponentString(TextFormatting.GOLD + "Sheets: " + TextFormatting.WHITE + tile.getPartColor(EnumColoredPart.SHEETS));
                    player.addChatMessage(message);
                }

                message = new TextComponentString(TextFormatting.RED + "Frame: " + TextFormatting.WHITE + tile.getPlankType());
                player.addChatMessage(message);
            } else
                onBedActivated(world, pos, state, player);
        }

        return true;
    }


    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (getTileForBed(worldIn, state, pos) == null) return state;

        TileWoodenBed bed = (TileWoodenBed) getTileForBed(worldIn, state, pos);
        boolean hl = hasBlanketsAndSheets(state, worldIn, pos);
        state = state.withProperty(BLANKETS, bed.getPartType(EnumColoredPart.BLANKETS));
        state = state.withProperty(SHEETS, bed.getPartType(EnumColoredPart.SHEETS));
        state = state.withProperty(STATUS,
            state.getValue(HEAD) ?
                (hl ? EnumBedPartStatus.HEAD_LINENS : EnumBedPartStatus.HEAD) :
                (hl ? EnumBedPartStatus.FOOT_LINENS : EnumBedPartStatus.FOOT));

        // TODO: Storage
        return state;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        // TODO: Check accuracy
        ItemStack stack = new ItemStack(BcbBlocks.woodenBed, 1, state.getBlock().getMetaFromState(state));

        TileWoodenBed tile = (TileWoodenBed) getTileForBed(world, state, pos);
        stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound stackTags = stack.getTagCompound();


        return stack;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (!state.getValue(HEAD)) return Collections.emptyList();

        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        ItemStack bedItem = new ItemStack(BcbItems.woodenBed);
        NBTTagCompound tags = new NBTTagCompound();
        state = getActualState(state, world, pos);
        TileWoodenBed twb = (TileWoodenBed) getTileForBed(world, state, pos);

        NBTTagCompound frameData = twb.getPlankData();
        tags.setString("frameType", frameData.getString("frameType"));
        tags.setInteger("frameMeta", frameData.getInteger("frameMeta"));
        bedItem.setTagCompound(tags);
        drops.add(bedItem);

        if(twb.getLinenPart(EnumColoredPart.BLANKETS, false) != null)
            drops.add(twb.getLinenPart(EnumColoredPart.BLANKETS, true));
        if(twb.getLinenPart(EnumColoredPart.SHEETS, false) != null)
            drops.add(twb.getLinenPart(EnumColoredPart.SHEETS, true));
        return drops;
    }
}
