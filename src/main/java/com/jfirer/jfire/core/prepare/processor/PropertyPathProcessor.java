package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.IniReader;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.PropertyPath;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.util.PrepareConstant;
import com.jfirer.jfire.util.Utils;

import java.util.Arrays;

public class PropertyPathProcessor implements ContextPrepare
{

    @Override
    public ApplicationContext.NeedRefresh prepare(ApplicationContext context)
    {
        ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
        AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        context.getAllBeanRegisterInfos().stream().filter(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).isAnnotationPresent(Configuration.class)).filter(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).isAnnotationPresent(PropertyPath.class)).flatMap(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).getAnnotations(PropertyPath.class).stream()).flatMap(propertyPath -> Arrays.stream(propertyPath.value())).forEach(path -> {
            IniReader.IniFile iniFile = Utils.processPath(path);
            for (String property : iniFile.keySet())
            {
                context.getEnv().putProperty(property, iniFile.getValue(property));
            }
        });
//        for (Class<?> each : context.getConfigurationClassSet())
//        {
//            AnnotationContext annotationContext = annotationContextFactory.get(each, classLoader);
//            if (annotationContext.isAnnotationPresent(PropertyPath.class))
//            {
//                for (PropertyPath propertyPath : annotationContext.getAnnotations(PropertyPath.class))
//                {
//                    for (String path : propertyPath.value())
//                    {
//                        IniReader.IniFile iniFile = Utils.processPath(path);
//                        for (String property : iniFile.keySet())
//                        {
//                            context.getEnv().putProperty(property, iniFile.getValue(property));
//                        }
//                    }
//                }
//            }
//        }
        return ApplicationContext.NeedRefresh.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.DEFAULT_ORDER;
    }
}
