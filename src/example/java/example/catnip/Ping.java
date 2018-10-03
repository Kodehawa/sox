package example.catnip;

import sox.command.catnip.Command;
import sox.command.catnip.Context;

public class Ping extends Command {
    @Override
    public void process(Context context) {
        context.send("Pong!");
    }
}
