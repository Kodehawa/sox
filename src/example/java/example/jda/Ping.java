package example.jda;

import sox.command.jda.Command;
import sox.command.jda.Context;

public class Ping extends Command {
    public void process(Context context) {
        context.send("Pong!");
    }
}
