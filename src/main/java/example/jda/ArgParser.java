package example.jda;

import sox.command.argument.ArgumentParseError;
import sox.command.argument.Parsers;
import sox.command.jda.Command;
import sox.command.jda.Context;

public class ArgParser extends Command {
    public ArgParser() {
        addErrorHandler((context, command, error) -> {
            if(error instanceof ArgumentParseError) {
                context.send("Please provide a valid integer as an argument");
                return true; //we handled the error
            }
            return false;
        });
    }

    @Override
    public void process(Context context) {
        context.send("Argument is " + context.argument(Parsers.parseInt()));
    }
}
