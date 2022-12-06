package com.jfirer.jfire.test.function.beanannotest;

import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.configuration.Bean;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;

import javax.annotation.Resource;
import java.util.Optional;

@Configuration
public class MyBeanImport
{
    @Resource
    private ApplicationContext context;

    @Bean(name = "person6")
    public Person importBean()
    {
        AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        Optional<Person> any = context.getAllBeanRegisterInfos().stream().filter(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).isAnnotationPresent(Configuration.class)).filter(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).isAnnotationPresent(MyImport.class)).map(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).getAnnotation(MyImport.class)).map(myImport -> {
            Person person = new Person();
            person.setName(myImport.name());
            return person;
        }).findAny();
        return any.orElse(null);
//        for (Class<?> each : context.getConfigurationClassSet())
//        {
//            AnnotationContext annotationContext = annotationContextFactory.get(each);
//            if (annotationContext.isAnnotationPresent(MyImport.class))
//            {
//                MyImport myImport = annotationContext.getAnnotation(MyImport.class);
//                Person   person   = new Person();
//                person.setName(myImport.name());
//                return person;
//            }
//        }
//        return null;
    }
}
