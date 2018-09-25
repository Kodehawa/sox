package example.jda;

import sox.command.AbstractCommand;
import sox.command.jda.Command;
import sox.command.jda.Context;

@AbstractCommand.Meta(name = "meta")
public class CommandMeta extends Command {
    @Override
    public void process(Context context) {
        context.send("!meta");
    }
}
