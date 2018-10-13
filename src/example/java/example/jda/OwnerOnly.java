package example.jda;

import sox.command.Meta;
import sox.command.jda.Command;
import sox.command.jda.Context;

@Meta(name = "owner")
public class OwnerOnly extends Command {
    @Override
    public void process(Context context) {
        context.send("This command is owner only");
    }
}