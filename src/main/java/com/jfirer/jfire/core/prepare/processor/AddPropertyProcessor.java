package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.AddProperty;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.util.PrepareConstant;

import java.util.Arrays;

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
