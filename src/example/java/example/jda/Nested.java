package example.jda;

import sox.command.jda.Command;
import sox.command.jda.Context;

public class Nested extends Command {
    public void process(Context context) {
        context.send("Run !nested sub");
    }

    public static class Sub extends Command {
        public void process(Context context) {
            context.send("This is a subcommand");
        }
    }
}
