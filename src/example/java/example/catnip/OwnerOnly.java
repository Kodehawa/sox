package example.catnip;

import sox.command.catnip.Command;
import sox.command.catnip.Context;
import sox.command.meta.Meta;

@Meta(name = "owner")
public class OwnerOnly extends Command {
    @Override
    public void process(Context context) {
        context.send("This command is owner only");
    }
}
