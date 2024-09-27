package com.jfirer.jfire.core.aop.impl.support.cache;

import com.jfirer.jfire.core.aop.impl.CacheEnhanceManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentMapCacheManager implements CacheEnhanceManager.CacheManager
{
    protected ConcurrentMap<String, CacheEnhanceManager.Cache> cacheMap = new ConcurrentHashMap<String, CacheEnhanceManager.Cache>();

    @Override
    public CacheEnhanceManager.Cache get(String name)
    {
        return cacheMap.computeIfAbsent(name, key -> new ConcurrentMapCache());
    }
}
