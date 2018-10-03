package example.catnip;

import sox.command.AbstractCommand;
import sox.command.catnip.Command;
import sox.command.catnip.Context;

@AbstractCommand.Meta(name = "meta")
public class CommandMeta extends Command {
    @Override
    public void process(Context context) {
        context.send("!meta");
    }
}
