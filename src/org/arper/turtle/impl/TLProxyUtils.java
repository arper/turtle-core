package org.arper.turtle.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.google.common.base.Supplier;
import com.google.common.reflect.Reflection;

public class TLProxyUtils {

    public static <T> T proxySuppliedObjects(Class<T> objectClass,
                                             final Supplier<? extends T> supplier) {
        return Reflection.newProxy(objectClass, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(supplier.get(), args);
            }
        });
    }

    public static <T> T proxySwallowException(Class<T> objectClass,
                                              final T object) {
        return Reflection.newProxy(objectClass, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    return method.invoke(object, args);
                } catch (Exception e) {
                    TLLogging.error("Caught exception during invocation", e);
                    return null;
                }
            }
        });
    }

    private TLProxyUtils() {
        /* do not instantiate */
    }
}
