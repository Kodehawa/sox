package example.jda;

import net.dv8tion.jda.core.JDABuilder;
import sox.JDASoxBuilder;
import sox.Sox;
import sox.autoregister.AutoRegister;
import sox.command.jda.PrefixProvider;

import javax.security.auth.login.LoginException;

public class Bot {
    public static void main(String[] args) throws LoginException {
        Sox sox = new JDASoxBuilder()
                .prefix(PrefixProvider.startingWith("!"))
                .build();

        AutoRegister.jda("example.jda").into(sox);

        new JDABuilder()
                .setToken(System.getenv("BOT_TOKEN"))
                .addEventListener(sox)
                .build();

        System.out.println("Try the following commands:");
        sox.commandManager().commands().keySet().forEach(c -> {
            System.out.println("!" + c);
        });
    }
}
