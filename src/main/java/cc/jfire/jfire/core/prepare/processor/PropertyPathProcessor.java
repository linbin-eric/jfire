package cc.jfire.jfire.core.prepare.processor;

import cc.jfire.baseutil.bytecode.support.AnnotationContext;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.ContextPrepare;
import cc.jfire.jfire.core.prepare.annotation.PropertyPath;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import cc.jfire.jfire.util.PrepareConstant;
import cc.jfire.jfire.util.Utils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class PropertyPathProcessor implements ContextPrepare
{
    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        ClassLoader               classLoader = Thread.currentThread().getContextClassLoader();
        AtomicReference<Class<?>> reference   = new AtomicReference();
        context.getAllBeanRegisterInfos().stream()//
               .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(Configuration.class, beanRegisterInfo.getType()))//
               .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(PropertyPath.class, beanRegisterInfo.getType()))//
               .map(beanRegisterInfo -> {
                   reference.set(beanRegisterInfo.getType());
                   return AnnotationContext.getAnnotation(PropertyPath.class, beanRegisterInfo.getType());
               })//
               .flatMap(propertyPath -> Arrays.stream(propertyPath.value()))//
               .forEach(path -> Utils.readYmlConfig(reference.get(), path, context));
        return ApplicationContext.FoundNewContextPrepare.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.DEFAULT_ORDER;
    }
}
