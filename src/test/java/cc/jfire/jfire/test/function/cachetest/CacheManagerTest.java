package cc.jfire.jfire.test.function.cachetest;

import cc.jfire.baseutil.Resource;
import cc.jfire.jfire.core.aop.impl.CacheEnhanceManager.Cache;
import cc.jfire.jfire.core.aop.impl.CacheEnhanceManager.CacheManager;


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
