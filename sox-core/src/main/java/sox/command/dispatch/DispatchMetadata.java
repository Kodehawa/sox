package sox.command.dispatch;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;
import sox.command.argument.Parser;
import sox.inject.Injector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

class DispatchMetadata {
    private final Handler[] handlers;

    DispatchMetadata(ParserRegistry registry, Class<?> commandClass) {
        this.handlers = Arrays.stream(commandClass.getDeclaredMethods())
                .filter(m -> !m.isBridge() && !m.isSynthetic() && Modifier.isPublic(m.getModifiers()))
                .filter(m -> m.getAnnotation(DispatchIgnore.class) == null)
                .filter(m -> m.getName().equals("process"))
                .sorted(Injector.EXECUTABLE_COMPARATOR)
                .map(m -> new Handler(m, registry.resolve(m)))
                .toArray(Handler[]::new);
        if(this.handlers.length == 0) {
            throw new IllegalStateException("Command " + commandClass + " has no valid handler methods");
        }
    }

    <C extends AbstractContext<C>, T extends AbstractCommand<C, T>> void dispatch(T command, C context) {
        for(Handler h : handlers) {
            if(h.handle(command, context.snapshot())) return;
        }
        command.noMatches(context);
    }

    private static class Handler {
        private final Method method;
        private final Parser<?>[] parsers;

        private Handler(Method method, Parser<?>[] parsers) {
            this.method = method;
            this.parsers = parsers;
        }

        boolean handle(AbstractCommand<?, ?> command, AbstractContext<?> context) {
            Object[] array = new Object[parsers.length];
            for(int i = 0; i < parsers.length; i++) {
                Optional<?> optional = context.tryArgument(parsers[i]);
                if(!optional.isPresent()) {
                    return false;
                }
                array[i] = optional.get();
            }
            try {
                method.invoke(command, array);
                return true;
            } catch(IllegalAccessException e) {
                throw new AssertionError(e);
            } catch(InvocationTargetException e) {
                Handler.<Error>uncheckedThrow(e.getCause());
                throw new AssertionError("should not be reached");
            }
        }

        @SuppressWarnings("unchecked")
        private static <T extends Throwable> void uncheckedThrow(Throwable throwable) throws T {
            throw (T)throwable;
        }
    }
}
