package example.catnip;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.Extension;
import sox.CatnipSoxBuilder;
import sox.Sox;
import sox.autoregister.AutoRegister;
import sox.command.ContextKey;
import sox.command.catnip.PrefixProvider;

public class Bot {
    private static final ContextKey<Long> START_TIME = new ContextKey<>(Long.class, 0L);

    public static void main(String[] args) {
        Sox sox = new CatnipSoxBuilder()
                .prefix(PrefixProvider.startingWith("!"))
                .commandFilter((context, command) -> {
                    if(command.meta("owner") != null) {
                        return context.message().author().id().equals("your id");
                    }
                    return true;
                })
                .beforeCommands((context, command) -> context.put(START_TIME, System.currentTimeMillis()))
                .afterCommands((context, command) -> {
                    long duration = System.currentTimeMillis() - context.get(START_TIME);
                    System.out.println("Command " + command.name() + " took " + duration + "ms to execute");
                })
                .build();

        AutoRegister.catnip("example.catnip").into(sox);

        Catnip.catnip(System.getenv("BOT_TOKEN"))
                .loadExtension((Extension) sox)
                .connect();

        System.out.println("Try the following commands:");
        sox.commandManager().commands().keySet().forEach(c -> {
            System.out.println("!" + c);
        });
    }
}
