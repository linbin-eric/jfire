package com.jfirer.jfire.core.aop.impl.support.cache;

import com.jfirer.jfire.core.aop.impl.CacheEnhanceManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentMapCache implements CacheEnhanceManager.Cache
{
    private final ConcurrentMap<String, Element> map = new ConcurrentHashMap<String, Element>();

    @Override
    public void put(String key, Object value, int timeToLive)
    {
        if (timeToLive == -1)
        {
            Element element = new Element(value, System.currentTimeMillis(), -1);
            map.put(key, element);
        }
        else
        {
            Element element = new Element(value, System.currentTimeMillis(), timeToLive * 1000L);
            map.put(key, element);
        }
    }

    @Override
    public Object get(String key)
    {
        Element element = map.get(key);
        if (element != null)
        {
            if (element.ttl == -1)
            {
                return element.value;
            }
            else if (System.currentTimeMillis() - element.time > element.ttl)
            {
                map.remove(key, element);
                return null;
            }
            else
            {
                return element.value;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public void remove(String key)
    {
        map.remove(key);
    }

    @Override
    public void clear()
    {
        map.clear();
    }

    class Element
    {
        private final Object value;
        private final long   time;
        private final long   ttl;

        public Element(Object value, long time, long ttl)
        {
            this.value = value;
            this.time = time;
            this.ttl = ttl;
        }
    }
}
