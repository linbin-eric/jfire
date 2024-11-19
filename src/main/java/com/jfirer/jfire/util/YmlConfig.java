package com.jfirer.jfire.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YmlConfig
{
    private Map<String, Object> config = new HashMap<>();

    /**
     * value的类型可能是String，也可能是List<String>，也可能是一个与Map相同的嵌套结构。只有这三种可能。
     *
     * @return
     */
    public Map<String, Object> fullPathConfig()
    {
        return config;
    }

    public void addProperty(String key, Object value)
    {
        if (value instanceof String || value instanceof List || value instanceof Map)
        {
            config.put(key, value);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
