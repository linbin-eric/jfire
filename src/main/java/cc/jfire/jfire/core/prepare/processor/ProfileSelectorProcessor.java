package cc.jfire.jfire.core.prepare.processor;

import cc.jfire.baseutil.STR;
import cc.jfire.baseutil.StringUtil;
import cc.jfire.baseutil.bytecode.support.AnnotationContext;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.ContextPrepare;
import cc.jfire.jfire.core.prepare.annotation.ProfileSelector;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import cc.jfire.jfire.util.PrepareConstant;
import cc.jfire.jfire.util.Utils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 通过注解ProfileSelector的 value 属性来引入动态名称的配置文件位置，并且在activePropertyName代表的属性被激活的时候，来解析动态名称的文件位置实际值，并且载入系统。
 */
public class ProfileSelectorProcessor implements ContextPrepare
{
    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        String activeAttribute = (String) context.getConfig().fullPathConfig().get(ProfileSelector.activePropertyName);
        if (StringUtil.isBlank(activeAttribute))
        {
            return ApplicationContext.FoundNewContextPrepare.NO;
        }
        AtomicReference<Class<?>> reference = new AtomicReference();
        context.getAllBeanRegisterInfos().stream()//
               .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(Configuration.class, beanRegisterInfo.getType()))//
               .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(ProfileSelector.class, beanRegisterInfo.getType()))//
               .map(beanRegisterInfo -> {
                   reference.set(beanRegisterInfo.getType());
                   return AnnotationContext.getAnnotation(ProfileSelector.class, beanRegisterInfo.getType());
               })//
               .flatMap(profileSelector -> Arrays.stream(profileSelector.value()))//
               .map(profile -> STR.format(profile, activeAttribute))//
               .forEach(path -> Utils.readYmlConfig(reference.get(), path, context));
        return ApplicationContext.FoundNewContextPrepare.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.PROFILE_SELECTOR_ORDER;
    }
}
