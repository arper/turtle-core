package org.arper.turtle.impl.swing;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TLSwingPluginLoader {

    public static List<TLSwingPlugin> loadPluginsForClasses(Iterable<? extends Class<?>> pluginClasses) {
        List<TLSwingPlugin> loaded = Lists.newArrayListWithCapacity(
                Iterables.size(pluginClasses));
        for (Class<?> pluginClass : pluginClasses) {
            if (!TLSwingPlugin.class.isAssignableFrom(pluginClass)) {
                /* TODO: error invalid plugin */
                continue;
            }

            @SuppressWarnings("unchecked") // safe because of isAssignableFrom check
            TLSwingPlugin plugin = loadPlugin((Class<? extends TLSwingPlugin>) pluginClass);
            if (plugin != null) {
                loaded.add(plugin);
            }
        }

        return loaded;
    }

    private static TLSwingPlugin loadPlugin(Class<? extends TLSwingPlugin> pluginClass) {
        TLSwingPlugin plugin;
        try {
            plugin = pluginClass.newInstance();
        } catch (Exception e) {
            /* TODO: error instantiation exception */
            return null;
        }

        return plugin;
    }

    public static List<TLSwingPlugin> loadPluginsForNames(Iterable<String> pluginClassNames) {
        Iterable<Class<?>> classes = Iterables.transform(pluginClassNames,
                new Function<String, Class<?>>() {
            @Override
            public Class<?> apply(@Nullable String input) {
                if (input == null) {
                    /* TODO: error null class name */
                    return null;
                }
                try {
                    return Class.forName(input);
                } catch (Exception e) {
                    /* TODO: log */
                    return null;
                }
            }
        });
        return loadPluginsForClasses(Iterables.filter(classes, Predicates.notNull()));
    }

}
