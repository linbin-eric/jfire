package cc.jfire.jfire.test.function.beanannotest;

import cc.jfire.baseutil.Resource;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.inject.notated.PropertyRead;
import cc.jfire.jfire.core.prepare.ContextPrepare;
import cc.jfire.jfire.core.prepare.annotation.ComponentScan;
import cc.jfire.jfire.core.prepare.annotation.Import;
import cc.jfire.jfire.core.prepare.annotation.condition.Conditional;
import cc.jfire.jfire.core.prepare.annotation.configuration.Bean;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;


@Configuration
@ComponentScan("cc.jfire.jfire.test.function.beanannotest")
@MyImport(name = "myimport")
@Import({HouseProvider.class, Data.NameProperty.class})
public class Data
{
    @Resource(name = "house")
    private House  house;
    @Resource(name = "house2")
    private House  house2;
    @PropertyRead("name")
    private String name;

    @Bean
    public Person person()
    {
        return new Person();
    }

    @Bean
    @Conditional(Person2Condition.class)
    public Person person2()
    {
        return new Person();
    }

    @Bean
    public Person person4()
    {
        Person person = new Person();
        person.setName(name);
        return person;
    }

    @Bean
    public Person person5()
    {
        Person person = new Person();
        person.setName(house.name());
        return person;
    }

    @Bean
    public Person person7()
    {
        Person person = new Person();
        person.setName(house2.name());
        return person;
    }

    public static class NameProperty implements ContextPrepare
    {

        @Override
        public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
        {
            context.getConfig().addProperty("name", "linbin");
            return ApplicationContext.FoundNewContextPrepare.NO;
        }

        @Override
        public int order()
        {
            return 0;
        }
    }
}
