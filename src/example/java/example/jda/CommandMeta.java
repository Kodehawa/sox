package example.jda;

import sox.command.Category;
import sox.command.Meta;
import sox.command.OverrideName;
import sox.command.jda.Command;
import sox.command.jda.Context;

@Category("example" /* category name */)
@OverrideName("meta" /* new name */)
@Meta(name = "some property", value = "some value")
public class CommandMeta extends Command {
    @Override
    public void process(Context context) {
        context.send("some property = " + meta("some property"));
        context.send("I'm in category " + category());
    }
}

