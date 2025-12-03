package cc.jfire.jfire.core.prepare.processor;

import cc.jfire.baseutil.bytecode.support.AnnotationContext;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.ContextPrepare;
import cc.jfire.jfire.core.prepare.annotation.AddProperty;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import cc.jfire.jfire.util.PrepareConstant;

import java.util.Arrays;

/**
 * 处理AddProperty注解的处理器
 */
public class AddPropertyProcessor implements ContextPrepare
{
    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        context.getAllBeanRegisterInfos().stream().filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(Configuration.class, beanRegisterInfo.getType()))//
               .flatMap(beanRegisterInfo -> AnnotationContext.getAnnotations(AddProperty.class, beanRegisterInfo.getType()).stream())//
               .flatMap(addProperty -> Arrays.stream(addProperty.value())).forEach(pair -> {
                   int index = pair.indexOf("=");
                   if (index != -1)
                   {
                       String property = pair.substring(0, index).trim();
                       String value    = pair.substring(index + 1).trim();
                       context.getConfig().addProperty(property, value);
                   }
               });
        return ApplicationContext.FoundNewContextPrepare.NO;
    }

    public int order()
    {
        return PrepareConstant.DEFAULT_ORDER;
    }
}
