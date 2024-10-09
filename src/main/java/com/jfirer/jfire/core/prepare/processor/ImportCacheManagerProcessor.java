package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.EnableCacheManager;
import com.jfirer.jfire.util.PrepareConstant;

import java.util.stream.Collectors;

public class ImportCacheManagerProcessor implements ContextPrepare
{
    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        context.getAllBeanRegisterInfos().stream()//
               .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(EnableCacheManager.class, beanRegisterInfo.getType()))//
               .map(beanRegisterInfo -> AnnotationContext.getAnnotation(EnableCacheManager.class, beanRegisterInfo.getType()))//
               //这边只能使用toList，因为通过代理创造出来的注解实例没有实现hashCode方法，
               .collect(Collectors.toList())//
               .stream()//
               .map(EnableCacheManager::value)//
               .forEach(context::register);
        return ApplicationContext.FoundNewContextPrepare.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.DEFAULT_ORDER;
    }
}
