package zornco.bedcraftbeyond.blocks;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import zornco.bedcraftbeyond.BedCraftBeyond;
import zornco.bedcraftbeyond.blocks.properties.PropertyString;
import zornco.bedcraftbeyond.blocks.tiles.TileColoredBed;
import zornco.bedcraftbeyond.client.colors.EnumBedFabricType;
import zornco.bedcraftbeyond.item.BcbItems;
import zornco.bedcraftbeyond.item.ItemBlanket;
import zornco.bedcraftbeyond.item.ItemSheets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/***
 * A colored bed has sheets and blankets that are separately dyeable.
 * It works the same as a regular bed otherwise.
 */
public class BlockWoodenBed extends BlockBedBase {

  public static PropertyBool HAS_STORAGE = PropertyBool.create("storage");
  public static PropertyString FRAME_TYPE = new PropertyString("frame");

  public static PropertyEnum<EnumBedFabricType> BLANKETS = PropertyEnum.create("color_blankets", EnumBedFabricType.class);
  public static PropertyEnum<EnumBedFabricType> SHEETS = PropertyEnum.create("color_sheets", EnumBedFabricType.class);

  public enum EnumColoredPart { BLANKETS, SHEETS, PLANKS }

  public BlockWoodenBed() {
    setRegistryName(BedCraftBeyond.MOD_ID, "colored_bed");
    setUnlocalizedName("beds.colored");
    setDefaultState(getDefaultState()
            .withProperty(FACING, EnumFacing.NORTH)
            .withProperty(OCCUPIED, false)
            .withProperty(HEAD, false)
            .withProperty(HAS_STORAGE, false)
            .withProperty(BLANKETS, EnumBedFabricType.NONE)
            .withProperty(SHEETS, EnumBedFabricType.NONE));
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new ExtendedBlockState(this, new IProperty[]{ HEAD, OCCUPIED, FACING, HAS_STORAGE, BLANKETS, SHEETS}, new IUnlistedProperty[]{ FRAME_TYPE } );
  }

  @Override
  public TileEntity createTileEntity(World world, IBlockState state) {
    return state.getValue(HEAD) ? new TileColoredBed() : null;
  }

  @Override
  public boolean hasTileEntity(IBlockState state) {
    return state.getValue(HEAD);
  }

  public static TileColoredBed getTileEntity(IBlockAccess world, BlockPos bedPos){
    TileEntity te = world.getTileEntity(bedPos);
    if(te instanceof TileColoredBed) return (TileColoredBed) te;

    IBlockState state = world.getBlockState(bedPos);
    if(!(state.getBlock() instanceof BlockWoodenBed)) return null;
    BlockPos actualTileHolder = bedPos.offset(state.getValue(BlockHorizontal.FACING));

    TileEntity realHolder = world.getTileEntity(actualTileHolder);
    if(realHolder == null || !(realHolder instanceof TileColoredBed)) return null;
    return (TileColoredBed) realHolder;
  }

  @Override
  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
    TileColoredBed tile = getTileEntity(world, pos);

    if (tile == null) return true;
    if (world.isRemote) return true;

    state = getActualState(state, world, pos);
    // Add/remove blankets and sheets

    if(heldItem != null) {
      if (heldItem.getItem() instanceof ItemBlanket){
        boolean set = tile.setLinenPart(EnumColoredPart.BLANKETS, heldItem);
        if(set){ --heldItem.stackSize; if(heldItem.stackSize < 1) player.setHeldItem(hand, null); }
      }

      if (heldItem.getItem() instanceof ItemSheets){
        boolean set = tile.setLinenPart(EnumColoredPart.SHEETS, heldItem);
        if(set){ --heldItem.stackSize; if(heldItem.stackSize < 1) player.setHeldItem(hand, null); }
      }
    }

    if(heldItem == null) {
      if (player.isSneaking()) {
        player.addChatMessage(new TextComponentString(tile.getBlanketsColor().name() + ", " + tile.getSheetsColor().name()));
      } else
        onBedActivated(world, pos, state, player);
    }

    return true;
  }

  @Override
  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    if(getTileEntity(worldIn, pos) == null) return state;

    TileColoredBed bed = getTileEntity(worldIn, pos);
    state = state.withProperty(BLANKETS, bed.getBlanketsColor());
    state = state.withProperty(SHEETS, bed.getSheetsColor());

    return state;
    // TODO: Add inventory and plank types
  }

  @Override
  public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
    // TODO: Check accuracy
    ItemStack stack = new ItemStack(BcbBlocks.woodenBed, 1, state.getBlock().getMetaFromState(state));

    TileColoredBed tile = getTileEntity(world, pos);
    stack.setTagCompound(new NBTTagCompound());
    NBTTagCompound stackTags = stack.getTagCompound();


    return stack;
  }

  @Override
  public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    // TODO: Implement blankets, sheets, and frame separate here
    if(!state.getValue(HEAD)) return Collections.emptyList();

    ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
    ItemStack bedItem = new ItemStack(BcbItems.coloredBed);
    NBTTagCompound tags = new NBTTagCompound();
    state = getActualState(state, world, pos);
    tags.setInteger("blankets", state.getValue(BLANKETS).getMetadata());
    tags.setInteger("sheets", state.getValue(SHEETS).getMetadata());
    tags.setString("frame", "minecraft:planks@0");
    bedItem.setTagCompound(tags);
    drops.add(bedItem);
    return drops;
  }
}