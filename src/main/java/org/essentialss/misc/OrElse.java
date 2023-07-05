package org.essentialss.misc;

import org.essentialss.api.utils.lamda.ThrowableFunction;
import org.essentialss.api.utils.lamda.ThrowableSupplier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public final class OrElse {

    private OrElse() {
        throw new RuntimeException("Should not create");
    }

    public static <O, I, R> R ifInstanceMap(O check, Class<I> instance, Function<I, R> is, Function<O, R> isNot) {
        if (instance.isInstance(check)) {
            return is.apply((I) check);
        }
        return isNot.apply(check);
    }

    public static <T extends Throwable, O, I, R> R ifInstanceMapThrowable(O check,
                                                                          Class<I> instance,
                                                                          ThrowableFunction<I, R, T> is,
                                                                          ThrowableFunction<O, R, T> isNot) throws T {
        if (instance.isInstance(check)) {
            return is.map((I) check);
        }
        return isNot.map(check);
    }

    public static <T extends Throwable, O, I, R> R ifInstanceMapThrowable(Class<T> clazz,
                                                                          O check,
                                                                          Class<I> instance,
                                                                          ThrowableFunction<I, R, T> is,
                                                                          ThrowableFunction<O, R, T> isNot) throws T {
        if (instance.isInstance(check)) {
            return is.map((I) check);
        }
        return isNot.map(check);
    }

    public static <T extends Throwable, R> R ifTry(Class<T> type, ThrowableSupplier<R, T> throwable, Supplier<R> getter) {
        try {
            return throwable.get();
        } catch (Throwable e) {
            if (type.isInstance(e)) {
                return getter.get();
            }
            throw new RuntimeException("error below", e);
        }
    }

    public static <O, I, R> R mapNull(@Nullable O check, Function<O, R> map, Supplier<R> whenNull) {
        if (null == check) {
            return whenNull.get();
        }
        return map.apply(check);
    }
}
