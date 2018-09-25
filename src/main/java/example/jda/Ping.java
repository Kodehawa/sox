package example.jda;

import sox.command.jda.Command;
import sox.command.jda.Context;

public class Ping extends Command {
    @Override
    public void process(Context context) {
        context.send("Pong!");
    }
}
