package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.AddProperty;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.util.PrepareConstant;

import java.util.Arrays;

public class AddPropertyProcessor implements ContextPrepare
{

    @Override
    public ApplicationContext.NeedRefresh prepare(ApplicationContext context)
    {
        AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        context.getAllBeanRegisterInfos().stream()
               .filter(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType())
                                                                   .isAnnotationPresent(Configuration.class))
                .flatMap(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).getAnnotations(AddProperty.class).stream())
                .flatMap(addProperty -> Arrays.stream(addProperty.value()))
                .forEach(pair->{
                    int index = pair.indexOf("=");
                    if (index != -1)
                    {
                        String property = pair.substring(0, index).trim();
                        String value    = pair.substring(index + 1).trim();
                        context.getEnv().putProperty(property, value);
                    }
                });
return ApplicationContext.NeedRefresh.NO;
//        for (Class<?> each : context.getConfigurationClassSet())
//        {
//            AnnotationContext annotationContext = annotationContextFactory.get(each);
//            if (annotationContext.isAnnotationPresent(AddProperty.class))
//            {
//                for (AddProperty addProperty : annotationContext.getAnnotations(AddProperty.class))
//                {
//                    for (String pair : addProperty.value())
//                    {
//                        int index = pair.indexOf("=");
//                        if (index != -1)
//                        {
//                            String property = pair.substring(0, index).trim();
//                            String value    = pair.substring(index + 1).trim();
//                            context.getEnv().putProperty(property, value);
//                        }
//                    }
//                }
//            }
//        }
//        return ApplicationContext.NeedRefresh.NO;
    }

    public int order()
    {
        return PrepareConstant.DEFAULT_ORDER;
    }
}
