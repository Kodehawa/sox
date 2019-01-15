package example.jda;

import net.dv8tion.jda.core.JDABuilder;
import sox.JDASoxBuilder;
import sox.Sox;
import sox.autoregister.AutoRegister;
import sox.command.ContextKey;
import sox.command.jda.PrefixProvider;

import javax.security.auth.login.LoginException;

public class Bot {
    private static final ContextKey<Long> START_TIME = new ContextKey<>(Long.class, 0L);

    public static void main(String[] args) throws LoginException {
        Sox sox = new JDASoxBuilder()
                .prefix(PrefixProvider.startingWith("!"))
                .commandFilter((context, command) -> {
                    if(command.meta("owner") != null) {
                        return context.author().getId().equals("your id");
                    }
                    return true;
                })
                .beforeCommands((context, command) -> context.put(START_TIME, System.currentTimeMillis()))
                .afterCommands((context, command) -> {
                    long duration = System.currentTimeMillis() - context.get(START_TIME);
                    System.out.println("Command " + command.name() + " took " + duration + "ms to execute");
                })
                .build();

        AutoRegister.jda("example.jda").into(sox);

        new JDABuilder(System.getenv("BOT_TOKEN"))
                .addEventListener(sox)
                .build();

        System.out.println("Try the following commands:");
        sox.commandManager().commands().keySet().forEach(c -> {
            System.out.println("!" + c);
        });
    }
}
