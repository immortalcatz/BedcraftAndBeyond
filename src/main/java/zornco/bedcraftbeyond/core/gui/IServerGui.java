package zornco.bedcraftbeyond.core.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IServerGui {
    Object getServerGui(World w, BlockPos pos, EntityPlayer player);
}