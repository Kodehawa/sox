package sox.command.jda.dispatch.config;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that lookup should happen only on the current shard.
 *
 * Used for
 * <ul>
 *     <li>{@link net.dv8tion.jda.core.entities.User users}</li>
 * </ul>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentShard {}
