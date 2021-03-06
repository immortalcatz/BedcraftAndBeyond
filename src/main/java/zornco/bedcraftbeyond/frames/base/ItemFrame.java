package zornco.bedcraftbeyond.frames.base;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemFrame extends ItemBlock {


    public ItemFrame(Block block) {
        super(block);
        setRegistryName(block.getRegistryName());
        setMaxStackSize(1);
    }

    protected void placeSimpleBedBlocks(World world, EntityPlayer player, BlockPos initPos, Block bedBlock, ItemStack placer) throws Exception {
        if(world.isRemote) return;

        BlockPos btmHalf = initPos;
        BlockPos topHalf = btmHalf.offset(player.getHorizontalFacing());

        IBlockState foot = bedBlock.getDefaultState().withProperty(BlockBedBase.FACING, player.getHorizontalFacing())
            .withProperty(BlockBedBase.HEAD, false);
        if (!world.setBlockState(btmHalf, foot, 3)) throw new Exception("Failed to set blockstate.");

        IBlockState head = bedBlock.getDefaultState()
            .withProperty(BlockBedBase.HEAD, true)
            .withProperty(BlockBedBase.FACING, player.getHorizontalFacing().getOpposite());
        if (!world.setBlockState(topHalf, head, 2)) throw new Exception("Failed to set blockstate.");
    }

    public static boolean testSimpleBedPlacement(World world, EntityPlayer player, BlockPos initialPosition, ItemStack placer) {
        if (!world.getBlockState(initialPosition).getBlock().isReplaceable(world, initialPosition))
            initialPosition = initialPosition.offset(EnumFacing.UP);

        BlockPos btmPos = initialPosition;

        IBlockState stateInEditing = world.getBlockState(btmPos);
        if (!stateInEditing.getBlock().isReplaceable(world, btmPos) && !world.isAirBlock(btmPos)) return false;

        EnumFacing playerFacing = player.getHorizontalFacing();

        BlockPos topPos = initialPosition.offset(playerFacing);
        if (!world.getBlockState(topPos).getBlock().isReplaceable(world, topPos) && !world.isAirBlock(topPos))
            return false;

        return true;
    }

    protected boolean placeBedBlock(ItemStack stack, World world, EntityPlayer player, BlockPos pos, IBlockState state, boolean blockUpdate){
        if (!world.setBlockState(pos, state, blockUpdate ? 3 : 2)) return false;

        IBlockState success = world.getBlockState(pos);
        if (success.getBlock() == this.block)
        {
            setTileEntityNBT(world, player, pos, stack);
            this.block.onBlockPlacedBy(world, pos, state, player, stack);
        }

        return true;
    }
}
