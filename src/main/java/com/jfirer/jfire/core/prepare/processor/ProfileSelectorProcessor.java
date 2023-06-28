package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.Formatter;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.ProfileSelector;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.util.PrepareConstant;
import com.jfirer.jfire.util.Utils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class ProfileSelectorProcessor implements ContextPrepare
{
    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        String activeAttribute = context.getEnv().getProperty(ProfileSelector.activePropertyName);
        if (StringUtil.isBlank(activeAttribute))
        {
            return ApplicationContext.FoundNewContextPrepare.NO;
        }
        AtomicReference<Class<?>> reference = new AtomicReference();
        context.getAllBeanRegisterInfos().stream()//
               .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(Configuration.class, beanRegisterInfo.getType()))//
               .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(ProfileSelector.class, beanRegisterInfo.getType()))//
               .map(beanRegisterInfo ->
                    {
                        reference.set(beanRegisterInfo.getType());
                        return AnnotationContext.getAnnotation(ProfileSelector.class, beanRegisterInfo.getType());
                    })//
               .flatMap(profileSelector -> Arrays.stream(profileSelector.value()))//
               .map(profile -> Formatter.format(profile, activeAttribute))//
               .forEach(path -> Utils.readPropertyFile(reference.get(), path, context));
        return ApplicationContext.FoundNewContextPrepare.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.PROFILE_SELECTOR_ORDER;
    }
}
