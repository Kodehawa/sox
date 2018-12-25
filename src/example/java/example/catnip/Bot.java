package example.catnip;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.Extension;
import sox.CatnipSoxBuilder;
import sox.Sox;
import sox.autoregister.AutoRegister;
import sox.command.catnip.PrefixProvider;
import sox.command.dispatch.DynamicCommandDispatcher;

public class Bot {
    public static void main(String[] args) {
        Sox sox = new CatnipSoxBuilder()
                .commandDispatcher(new DynamicCommandDispatcher())
                .prefix(PrefixProvider.startingWith("!"))
                .commandFilter((context, command) -> {
                    if(command.meta("owner") != null) {
                        return context.message().author().id().equals("your id");
                    }
                    return true;
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
