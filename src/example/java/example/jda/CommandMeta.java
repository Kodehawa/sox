package example.jda;

import sox.command.jda.Command;
import sox.command.jda.Context;
import sox.command.meta.Alias;
import sox.command.meta.Category;
import sox.command.meta.Description;
import sox.command.meta.Meta;
import sox.command.meta.OverrideName;
import sox.command.meta.Usage;

@Category("example" /* category name */)
@OverrideName("meta" /* new name */)
@Meta(name = "some property", value = "some value")
@Description("example command meta usage")
@Usage("!meta or !example-alias")
@Alias("example-alias")
public class CommandMeta extends Command {
    @Override
    public void process(Context context) {
        context.send("some property = " + meta("some property"));
        context.send("I'm in category " + category());
        context.send("My description is " + description());
        context.send("My usage is " + usage());
    }
}

