package com.jfirer.jfire.core.aop.impl.support.cache;

import com.jfirer.jfire.core.aop.impl.CacheEnhanceManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractCacheManager implements CacheEnhanceManager.CacheManager
{
    protected List<String>                                     cacheNames = new LinkedList<String>();
    protected ConcurrentMap<String, CacheEnhanceManager.Cache> cacheMap   = new ConcurrentHashMap<String, CacheEnhanceManager.Cache>();

    public AbstractCacheManager()
    {
        cacheNames.add("default");
    }

    @Override
    public CacheEnhanceManager.Cache get(String name)
    {
        return cacheMap.get(name);
    }
}
