package cc.jfire.jfire.core.prepare.annotation;

import cc.jfire.jfire.core.aop.impl.CacheEnhanceManager;
import cc.jfire.jfire.core.aop.impl.support.cache.ConcurrentMapCacheManager;
import cc.jfire.jfire.core.prepare.processor.ImportCacheManagerProcessor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ImportCacheManagerProcessor.class)
public @interface EnableCacheManager
{
    Class<? extends CacheEnhanceManager.CacheManager> value() default ConcurrentMapCacheManager.class;
}
