package example.jda;

import sox.command.dispatch.DispatchIgnore;
import sox.command.dispatch.config.NotEmpty;
import sox.command.jda.Command;
import sox.command.jda.Context;

import java.util.List;

public class DynamicDispatch extends Command {
    public void process(@NotEmpty List<Integer> integers) {
        System.out.println("integers = " + integers);
    }

    public void process(String arg, Context context) {
        context.send("arg = " + arg + ". also context can go anywhere or even omitted");
    }

    @DispatchIgnore
    public void process() {
        System.out.println("this method will be ignored by the dispatcher");
    }

    @Override
    public void process(Context context) {
        context.send("default case");
    }
}