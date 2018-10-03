package example.catnip;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.Extension;
import sox.CatnipSoxBuilder;
import sox.Sox;
import sox.autoregister.AutoRegister;
import sox.command.catnip.PrefixProvider;

public class Bot {
    public static void main(String[] args) {
        Sox sox = new CatnipSoxBuilder()
                .prefix(PrefixProvider.startingWith("!"))
                .build();

        AutoRegister.catnip("example.catnip").into(sox);

        Catnip.catnip(System.getenv("BOT_TOKEN"))
                .loadExtension((Extension)sox)
                .startShards();

        System.out.println("Try the following commands:");
        sox.commandManager().commands().keySet().forEach(c -> {
            System.out.println("!" + c);
        });
    }
}
