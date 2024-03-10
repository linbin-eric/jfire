package com.jfirer.jfire.core;

import java.util.*;

public interface Environment
{
    void putProperty(String property, Object value);

    void addProperties(Properties properties);

    Object getProperty(String propertyName);

    Collection<String> properties();

    class EnvironmentImpl implements Environment
    {
        private final Map<String, Object> store = new HashMap<>();

        @Override
        public void putProperty(String property, Object value)
        {
            store.put(property, value);
        }

        @Override
        public void addProperties(Properties properties)
        {
            Enumeration<?> enumeration = properties.propertyNames();
            while (enumeration.hasMoreElements()) {
                String name = (String) enumeration.nextElement();
                store.put(name, properties.get(name));
            }
        }

        @Override
        public Object getProperty(String propertyName)
        {
            return store.get(propertyName);
        }

        @Override
        public Collection<String> properties()
        {
            return store.keySet();
        }
    }
}
