package sox.command.hook;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Hook for command calls. Can do pre-command and post-command tasks, abort command execution and handle errors.
 *
 * @param <C> Type of the context implementation.
 *
 * @apiNote {@link #shouldRunCommand(AbstractContext, AbstractCommand) shouldRunCommand} executes
 *          global hooks <b>before</b> any command specific hooks. All other callbacks execute
 *          command specific first.
 */
public interface CommandHook<C extends AbstractContext<C>> {
    /**
     * Called after a command executes <b>successfully</b>.
     *
     * @param context Context for the command call.
     * @param command Command called.
     */
    void afterCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command);

    /**
     * Called before a command executes, if all filters allowed it to execute.
     *
     * @param context Context for the command call.
     * @param command Command being called.
     */
    void beforeCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command);

    /**
     * Filters whether or not the command call should be allowed. If any hook returns false, the command
     * call is aborted.
     *
     * @param context Context for the command call.
     * @param command Command being called.
     *
     * @return False if the command execution should be aborted.
     */
    @CheckReturnValue
    boolean shouldRunCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command);

    /**
     * Called when a command throws an exception.
     *
     * @param context Context for the command call.
     * @param command Command being called.
     * @param e Exception thrown.
     *
     * @return True, if the exception was handled. Returning true means <b>no other exception hooks will be called</b>.
     */
    @CheckReturnValue
    boolean onCommandError(@Nonnull C context, @Nonnull AbstractCommand<C> command, @Nonnull Exception e);

    /**
     * Creates a hook from an {@link AfterCommand after command} action.
     *
     * @param after Action to run after command execution.
     * @param <C> Type of the context implementation.
     *
     * @return A hook that delegates to the provided callback.
     */
    @Nonnull
    @CheckReturnValue
    static <C extends AbstractContext<C>> CommandHook<C> fromAfter(@Nonnull AfterCommand<C> after) {
        return new AbstractCommandHook<C>() {
            @Override
            public void afterCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command) {
                after.afterCommand(context, command);
            }
        };
    }

    /**
     * Creates a hook from a {@link BeforeCommand before command} action.
     *
     * @param before Action to run before command execution.
     * @param <C> Type of the context implementation.
     *
     * @return A hook that delegates to the provided callback.
     */
    @Nonnull
    @CheckReturnValue
    static <C extends AbstractContext<C>> CommandHook<C> fromBefore(@Nonnull BeforeCommand<C> before) {
        return new AbstractCommandHook<C>() {
            @Override
            public void beforeCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command) {
                before.beforeCommand(context, command);
            }
        };
    }

    /**
     * Creates a hook from a {@link CommandFilter command filter}.
     *
     * @param filter Filter for command calls.
     * @param <C> Type of the context implementation.
     *
     * @return A hook that delegates to the provided callback.
     */
    @Nonnull
    @CheckReturnValue
    static <C extends AbstractContext<C>> CommandHook<C> fromFilter(@Nonnull CommandFilter<C> filter) {
        return new AbstractCommandHook<C>() {
            @Override
            public boolean shouldRunCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command) {
                return filter.shouldRunCommand(context, command);
            }
        };
    }

    /**
     * Creates a hook from an {@link CommandErrorHandler error handler}.
     *
     * @param errorHandler Handler for any exceptions thrown by commands.
     * @param <C> Type of the context implementation.
     *
     * @return A hook that delegates to the provided callback.
     */
    @Nonnull
    @CheckReturnValue
    static <C extends AbstractContext<C>> CommandHook<C> fromErrorHandler(@Nonnull CommandErrorHandler<C> errorHandler) {
        return new AbstractCommandHook<C>() {
            @Override
            public boolean onCommandError(@Nonnull C context, @Nonnull AbstractCommand<C> command, @Nonnull Exception e) {
                return errorHandler.onCommandError(context, command, e);
            }
        };
    }
}
