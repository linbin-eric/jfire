package com.jfirer.jfire.core.prepare.annotation;

import com.jfirer.jfire.core.aop.impl.CacheEnhanceManager;
import com.jfirer.jfire.core.aop.impl.support.cache.ConcurrentMapCacheManager;
import com.jfirer.jfire.core.prepare.processor.ImportCacheManagerProcessor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ImportCacheManagerProcessor.class)
public @interface EnableCacheManager
{
    Class<? extends CacheEnhanceManager.CacheManager> value() default ConcurrentMapCacheManager.class;
}
