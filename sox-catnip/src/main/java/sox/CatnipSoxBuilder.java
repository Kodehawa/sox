package sox;

import com.mewna.catnip.entity.message.Message;
import sox.command.catnip.CatnipReflectiveCommandManager;
import sox.command.catnip.Command;
import sox.command.catnip.Context;
import sox.command.catnip.PrefixProvider;
import sox.impl.CatnipSoxImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CatnipSoxBuilder extends SoxBuilder<Message, Context, Command, CatnipSoxBuilder> {
    private static final List<PrefixProvider> DEFAULT_PREFIX = Collections.singletonList(PrefixProvider.mention());

    private List<PrefixProvider> prefixProviders = DEFAULT_PREFIX;
    private String deploymentID = "sox";

    public CatnipSoxBuilder() {
        super(CatnipReflectiveCommandManager::new);
        commandFilter((context, command) -> !command.guildOnly() || !context.isDM());
    }

    @Nonnull
    @CheckReturnValue
    public CatnipSoxBuilder prefixes(@Nonnull PrefixProvider... providers) {
        if(providers.length == 0) {
            throw new IllegalArgumentException("No providers specified!");
        }
        this.prefixProviders = Arrays.asList(providers);
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public CatnipSoxBuilder prefix(@Nonnull PrefixProvider provider) {
        return prefixes(provider);
    }

    @Nonnull
    @CheckReturnValue
    public CatnipSoxBuilder deploymentID(@Nonnull String deploymentID) {
        this.deploymentID = deploymentID;
        return this;
    }

    @Override
    @Nonnull
    @CheckReturnValue
    protected CatnipSoxImpl newInstance() {
        return new CatnipSoxImpl(prefixProviders, deploymentID);
    }
}
