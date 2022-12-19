package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.ProfileSelector;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.util.PrepareConstant;
import com.jfirer.jfire.util.Utils;

public class ProfileSelectorProcessor implements ContextPrepare
{

    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        String activeAttribute = context.getEnv().getProperty(ProfileSelector.activePropertyName);
        if (!StringUtil.isNotBlank(activeAttribute))
        {
            return ApplicationContext.FoundNewContextPrepare.NO;
        }
        AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        context.getAllBeanRegisterInfos().stream()//
               .filter(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).isAnnotationPresent(Configuration.class))//
               .filter(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).isAnnotationPresent(ProfileSelector.class))//
               .map(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).getAnnotation(ProfileSelector.class))//
               .map(profileSelector -> profileSelector.protocol() + ":" + profileSelector.prefix() + activeAttribute + ".ini")//
               .map(profileFileName -> Utils.processPath(profileFileName))//
               .forEach(iniFile -> {
                   for (String key : iniFile.keySet())
                   {
                       context.getEnv().putProperty(key, iniFile.getValue(key));
                   }
               });
        return ApplicationContext.FoundNewContextPrepare.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.PROFILE_SELECTOR_ORDER;
    }
}
