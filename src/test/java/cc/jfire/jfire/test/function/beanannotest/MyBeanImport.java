package cc.jfire.jfire.test.function.beanannotest;

import cc.jfire.baseutil.Resource;
import cc.jfire.baseutil.bytecode.support.AnnotationContext;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.configuration.Bean;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;

import java.util.Optional;

@Configuration
public class MyBeanImport
{
    @Resource
    private ApplicationContext context;

    @Bean(name = "person6")
    public Person importBean()
    {
        Optional<Person> any = context.getAllBeanRegisterInfos().stream().filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(Configuration.class, beanRegisterInfo.getType())).filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(MyImport.class, beanRegisterInfo.getType())).map(beanRegisterInfo -> AnnotationContext.getAnnotation(MyImport.class, beanRegisterInfo.getType())).map(myImport -> {
            Person person = new Person();
            person.setName(myImport.name());
            return person;
        }).findAny();
        return any.orElse(null);
    }
}
