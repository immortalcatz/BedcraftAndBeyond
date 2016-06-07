package zornco.bedcraftbeyond.common.commands.fragments;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import zornco.bedcraftbeyond.BedCraftBeyond;
import zornco.bedcraftbeyond.common.commands.CommandFragment;
import zornco.bedcraftbeyond.common.commands.FramesCommandsUtils;
import zornco.bedcraftbeyond.common.frames.FrameRegistry;
import zornco.bedcraftbeyond.common.frames.FrameWhitelist;

import java.util.*;

public class FragmentFrames extends CommandFragment {

    public static void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new CommandException(BedCraftBeyond.MOD_ID + ".errors.no_subcommand");
        switch (args[1].toLowerCase()) {
            case "list":
                handleList(sender, args);
                break;

            case "add":
            case "remove":
                FragmentFramesModify.execute(server, sender, args);
                break;

            case "check":
                FragmentFramesCheck.execute(server, sender, args);
                break;

            case "reload":
                sender.addChatMessage(new TextComponentTranslation(BedCraftBeyond.MOD_ID + ".messages.reloadingFrames"));
                BedCraftBeyond.PROXY.compileFrames();
                break;
        }
    }

    public static List<String> getTabOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        switch (args.length) {
            case 2:
                return CommandBase.getListOfStringsMatchingLastWord(args, ImmutableList.of("add", "remove", "check", "reload", "list"));
            default:
                switch (args[1].toLowerCase()) {
                    case "add":
                    case "remove":
                        return FragmentFramesModify.getTabOptions(server, sender, args, pos);

                    case "reload":
                        return Collections.emptyList();

                    case "check":
                        return FragmentFramesCheck.getTabOptions(server, sender, args, pos);

                    case "list":
                        return CommandBase.getListOfStringsMatchingLastWord(args, FrameRegistry.EnumFrameType.getPossible());
                }

                return Collections.emptyList();
        }
    }

    // bedcraft frames list <part>
    private static void handleList(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 3) throw new CommandException(BedCraftBeyond.MOD_ID + ".errors.frames.no_frame_type");
        FrameRegistry.EnumFrameType type = FramesCommandsUtils.getFrameTypeFromArgs(args, 2);
        if(type == FrameRegistry.EnumFrameType.UNKNOWN) throw new CommandException(BedCraftBeyond.MOD_ID + ".frames.errors.unknown_frame_type");

        FrameWhitelist frames = FrameRegistry.getFrameWhitelist(type);
        sender.addChatMessage(new TextComponentString(frames.getTerminalOutput()));
    }
}