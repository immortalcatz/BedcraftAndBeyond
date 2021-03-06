package zornco.bedcraftbeyond.rugs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zornco.bedcraftbeyond.core.BedCraftBeyond;
import zornco.bedcraftbeyond.core.ModContent;

import java.util.List;

public class BlockRug extends Block {

    public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.<EnumDyeColor>create("color", EnumDyeColor.class);
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyEnum<BlockRug.EnumShape> SHAPE = PropertyEnum.<BlockRug.EnumShape>create("shape", BlockRug.EnumShape.class);
  /*public static final PropertyEnum<BlockRug.EnumSideHalf> EL = PropertyEnum.<BlockRug.EnumSideHalf>create("el", BlockRug.EnumSideHalf.class);
    public static final PropertyEnum<BlockRug.EnumSideHalf> SL = PropertyEnum.<BlockRug.EnumSideHalf>create("sl", BlockRug.EnumSideHalf.class);
	public static final PropertyEnum<BlockRug.EnumSideHalf> WL = PropertyEnum.<BlockRug.EnumSideHalf>create("wl", BlockRug.EnumSideHalf.class);
	public static final PropertyEnum<BlockRug.EnumSideHalf> NL = PropertyEnum.<BlockRug.EnumSideHalf>create("nl", BlockRug.EnumSideHalf.class);*/
	/*public static final PropertyEnum<BlockRug.EnumSideHalf> EU = PropertyEnum.<BlockRug.EnumSideHalf>create("eu", BlockRug.EnumSideHalf.class);
	public static final PropertyEnum<BlockRug.EnumSideHalf> SU = PropertyEnum.<BlockRug.EnumSideHalf>create("su", BlockRug.EnumSideHalf.class);
	public static final PropertyEnum<BlockRug.EnumSideHalf> WU = PropertyEnum.<BlockRug.EnumSideHalf>create("wu", BlockRug.EnumSideHalf.class);
	public static final PropertyEnum<BlockRug.EnumSideHalf> NU = PropertyEnum.<BlockRug.EnumSideHalf>create("nu", BlockRug.EnumSideHalf.class);*/

    public BlockRug() {
        super(Material.CLOTH);
        setHardness(0.8f);
        setUnlocalizedName(BedCraftBeyond.MOD_ID + ".rug");
        setRegistryName(BedCraftBeyond.MOD_ID, "rug_block");

        this.setDefaultState(this.blockState.getBaseState()
            .withProperty(COLOR, EnumDyeColor.WHITE)
            .withProperty(FACING, EnumFacing.NORTH)
            .withProperty(SHAPE, BlockRug.EnumShape.STRAIGHT));

        this.setCreativeTab(BedCraftBeyond.MAIN_TAB);
        GameRegistry.register(this);
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        state = getActualState(state, source, pos);
        switch (state.getValue(SHAPE)) {
            case STRAIGHT:
            case INNER_LEFT:
            case INNER_RIGHT:
            case OUTER_LEFT:
            case OUTER_RIGHT:
                return new AxisAlignedBB(0, -1, 0, 1, -1 + 0.5625, 1);
            case BLOCK:
                return new AxisAlignedBB(0, 0, 0, 1, 0.0625, 1);
            case SLAB:
                return new AxisAlignedBB(0, 0.5, 0, 1, 0.5625, 1);
            default:
                return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        }
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    /**
     * checks to see if you can place this block can be placed on that side of a
     * block: BlockLever overrides
     */
    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP)
            || this.isBlockValid(worldIn.getBlockState(pos.down()).getBlock());

    }

    /**
     * Checks to see if its valid to put this block at the specified
     * coordinates. Args: world, pos
     */
    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP)
            || this.isBlockValid(worldIn.getBlockState(pos.down()).getBlock());//(pos.down()));

    }

    public static boolean isBlockValid(Block block) {
        return (block instanceof BlockStairs) || (block instanceof BlockSlab);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
        if (!worldIn.isSideSolid(pos.down(), EnumFacing.DOWN) || !isBlockValid(worldIn.getBlockState(pos.down()).getBlock())) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }

    /**
     * Return whether an adjacent block can connect to a wall.
     */
    public static boolean canConnectRugTo(IBlockAccess par1IBlockAccess, BlockPos pos) {
        Block var5 = par1IBlockAccess.getBlockState(pos).getBlock();

        return (var5 == ModContent.Blocks.stoneBed) && !par1IBlockAccess.isAirBlock(pos) ? true : false;

    }

    /**
     * Determines the damage on the item the block drops. Used in cloth and
     * wood.
     */
    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(COLOR).getMetadata();
    }

    /**
     * Takes a dye damage value and returns the block damage value to match
     */
    public static int getBlockFromDye(int par0) {
        return ~par0 & 15;
    }

    /**
     * Takes a block damage value and returns the dye damage value to match
     */
    public static int getDyeFromBlock(int par0) {
        return ~par0 & 15;
    }

    @SuppressWarnings("rawtypes")
    @Override
    @SideOnly(Side.CLIENT)
    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IBlockState state) {
        return ((EnumDyeColor) state.getValue(COLOR)).getMapColor();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state) {
        return ((EnumDyeColor) state.getValue(COLOR)).getMetadata();
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        //get state of block underneath
        IBlockState underState = worldIn.getBlockState(pos.down());
        Block underBlock = underState.getBlock();
        underState = underBlock.getActualState(underState, worldIn, pos.down());

        // Stairs - facing up
        if (underBlock instanceof BlockStairs && underState.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.BOTTOM) {
            state = state.withProperty(SHAPE, BlockRug.EnumShape.getShape(underState.getValue(BlockStairs.SHAPE)));
            state = state.withProperty(FACING, underState.getValue(FACING));
            // Slab - facing up
        } else if (underBlock instanceof BlockSlab && underState.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM) {
            state = state.withProperty(SHAPE, BlockRug.EnumShape.SLAB);
            // Otherwise, use carpet style
        } else {
            state = state.withProperty(SHAPE, BlockRug.EnumShape.BLOCK);
        }

        if (state.getValue(SHAPE) == BlockRug.EnumShape.SLAB) {
            //state = applyLowerState(state, worldIn, pos.east(), EL, EnumFacing.EAST);
            //state = applyLowerState(state, worldIn, pos.south(), SL, EnumFacing.SOUTH);
            //state = applyLowerState(state, worldIn, pos.west(), WL, EnumFacing.WEST);
            //state = applyLowerState(state, worldIn, pos.north(), NL, EnumFacing.NORTH);
        }

        return state;
    }

    /**
     * This method WOULD apply the states for making the rug go up other blocks, but those states made the number of block states skyrocket :c
     *
     * @param state
     * @param worldIn
     * @param pos
     * @param side
     * @param facing
     * @return
     */
    private IBlockState applyLowerState(IBlockState state, IBlockAccess worldIn, BlockPos pos, PropertyEnum<EnumSideHalf> side, EnumFacing facing) {
        IBlockState sideState;
        Block sideBlock;
        sideState = worldIn.getBlockState(pos);
        sideBlock = sideState.getBlock();
        sideState = sideBlock.getActualState(sideState, worldIn, pos);
        if (sideBlock instanceof BlockRug) {
            if (sideState.getValue(SHAPE) == BlockRug.EnumShape.BLOCK) {
                state = state.withProperty(side, BlockRug.EnumSideHalf.HALF);
            } else if (sideState.getValue(SHAPE) == BlockRug.EnumShape.STRAIGHT) {
                BlockRug.EnumSideHalf apply = BlockRug.EnumSideHalf.NONE;
                ;
                switch (facing) {
                    case EAST:
                        switch (sideState.getValue(FACING)) {
                            case SOUTH:
                                apply = BlockRug.EnumSideHalf.RIGHT;
                                break;
                            case WEST:
                                apply = BlockRug.EnumSideHalf.HALF;
                                break;
                            case NORTH:
                                apply = BlockRug.EnumSideHalf.LEFT;
                                break;
                            default:
                                break;
                        }
                        break;
                    case SOUTH:
                        switch (sideState.getValue(FACING)) {
                            case EAST:
                                apply = BlockRug.EnumSideHalf.LEFT;
                                break;
                            case WEST:
                                apply = BlockRug.EnumSideHalf.RIGHT;
                                break;
                            case NORTH:
                                apply = BlockRug.EnumSideHalf.HALF;
                                break;
                            default:
                                break;
                        }
                        break;
                    case WEST:
                        switch (sideState.getValue(FACING)) {
                            case EAST:
                                apply = BlockRug.EnumSideHalf.HALF;
                                break;
                            case SOUTH:
                                apply = BlockRug.EnumSideHalf.LEFT;
                                break;
                            case WEST:
                                apply = BlockRug.EnumSideHalf.HALF;
                                break;
                            case NORTH:
                                apply = BlockRug.EnumSideHalf.RIGHT;
                                break;
                            default:
                                break;
                        }
                        break;
                    case NORTH:
                        switch (sideState.getValue(FACING)) {
                            case EAST:
                                apply = BlockRug.EnumSideHalf.RIGHT;
                                break;
                            case SOUTH:
                                apply = BlockRug.EnumSideHalf.HALF;
                                break;
                            case WEST:
                                apply = BlockRug.EnumSideHalf.LEFT;
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }


                state = state.withProperty(side, apply);
            }
        }
        return state;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{COLOR, FACING, SHAPE});//, EL, SL, WL, NL});//, EU, SU, WU, NU });
    }

    public enum EnumSideHalf implements IStringSerializable {
        HALF("half"),
        LEFT("left"),
        RIGHT("right"),
        FULL("full"),
        NONE("none");

        private final String name;

        EnumSideHalf(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }
    }

    public enum EnumShape implements IStringSerializable {
        BLOCK("block"),
        SLAB("slab"),
        STRAIGHT("straight"),
        INNER_LEFT("inner_left"),
        INNER_RIGHT("inner_right"),
        OUTER_LEFT("outer_left"),
        OUTER_RIGHT("outer_right");

        private final String name;

        EnumShape(String name) {
            this.name = name;
        }

        public static BlockRug.EnumShape getShape(BlockStairs.EnumShape shape) {
            switch (shape) {
                case STRAIGHT:
                    return BlockRug.EnumShape.STRAIGHT;
                case INNER_LEFT:
                    return BlockRug.EnumShape.INNER_LEFT;
                case INNER_RIGHT:
                    return BlockRug.EnumShape.INNER_RIGHT;
                case OUTER_LEFT:
                    return BlockRug.EnumShape.OUTER_LEFT;
                case OUTER_RIGHT:
                    return BlockRug.EnumShape.OUTER_RIGHT;

                default:
                    return BlockRug.EnumShape.STRAIGHT;
            }
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }
    }
}
