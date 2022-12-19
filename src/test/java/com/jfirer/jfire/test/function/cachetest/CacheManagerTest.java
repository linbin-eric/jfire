package com.jfirer.jfire.test.function.cachetest;

import com.jfirer.jfire.core.aop.impl.CacheEnhanceManager.Cache;
import com.jfirer.jfire.core.aop.impl.CacheEnhanceManager.CacheManager;

import javax.annotation.Resource;

@Resource
public class CacheManagerTest implements CacheManager
{

    private final Cache cahce = new DemoCache();

    @Override
    public Cache get(String name)
    {
        System.out.println("cache名称：" + name);
        return cahce;
    }
}
