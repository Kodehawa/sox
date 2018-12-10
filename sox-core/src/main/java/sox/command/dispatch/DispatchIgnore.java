package sox.command.dispatch;

import sox.command.AbstractContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a method should be ignored by the {@link DynamicCommandDispatcher dispatcher}.
 * Does not affect the {@link sox.command.AbstractCommand#process(AbstractContext) default case}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DispatchIgnore {}
