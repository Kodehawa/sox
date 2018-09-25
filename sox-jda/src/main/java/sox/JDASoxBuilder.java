package sox;

import net.dv8tion.jda.core.entities.Message;
import sox.command.jda.JDAReflectiveCommandManager;
import sox.command.jda.PrefixProvider;
import sox.command.jda.Context;
import sox.impl.JDASoxImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JDASoxBuilder extends SoxBuilder<Message, Context, JDASoxBuilder> {
    private static final List<PrefixProvider> DEFAULT_PREFIX = Collections.singletonList(PrefixProvider.mention());

    private List<PrefixProvider> prefixProviders = DEFAULT_PREFIX;

    public JDASoxBuilder() {
        super(JDAReflectiveCommandManager::new);
    }

    @Nonnull
    @CheckReturnValue
    public JDASoxBuilder prefixes(@Nonnull PrefixProvider... providers) {
        if(providers.length == 0) {
            throw new IllegalArgumentException("No providers specified!");
        }
        this.prefixProviders = Arrays.asList(providers);
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public JDASoxBuilder prefix(@Nonnull PrefixProvider provider) {
        return prefixes(provider);
    }

    @Override
    @Nonnull
    @CheckReturnValue
    protected JDASoxImpl newInstance() {
        return new JDASoxImpl(prefixProviders);
    }
}
