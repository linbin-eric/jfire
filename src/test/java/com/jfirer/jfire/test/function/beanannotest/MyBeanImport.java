package com.jfirer.jfire.test.function.beanannotest;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
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
        Optional<Person> any = context.getAllBeanRegisterInfos().stream().filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(Configuration.class,beanRegisterInfo.getType())).filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(MyImport.class,beanRegisterInfo.getType())).map(beanRegisterInfo -> AnnotationContext.getAnnotation(MyImport.class,beanRegisterInfo.getType())).map(myImport -> {
            Person person = new Person();
            person.setName(myImport.name());
            return person;
        }).findAny();
        return any.orElse(null);
    }
}
