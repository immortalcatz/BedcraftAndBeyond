package zornco.bedcraftbeyond.frames.registry;

import net.minecraft.command.CommandException;

public class FrameException extends CommandException {

    public FrameException(String message) {
        super(message);
    }

    public FrameException(String message, Object... params) {
        super(message, params);
    }
}
