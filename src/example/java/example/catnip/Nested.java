package example.catnip;

import sox.command.catnip.Command;
import sox.command.catnip.Context;

public class Nested extends Command {
    @Override
    public void process(Context context) {
        context.send("Run !nested sub");
    }

    public static class Sub extends Command {
        @Override
        public void process(Context context) {
            context.send("This is a subcommand");
        }
    }
}
