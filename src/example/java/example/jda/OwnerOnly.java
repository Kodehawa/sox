package example.jda;

import sox.command.jda.Command;
import sox.command.jda.Context;
import sox.command.meta.Meta;

@Meta(name = "owner")
public class OwnerOnly extends Command {
    public void process(Context context) {
        context.send("This command is owner only");
    }
}