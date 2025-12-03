package cc.jfire.jfire.test.function.loader;

import cc.jfire.baseutil.Resource;
import cc.jfire.jfire.core.bean.BeanDefinition;
import cc.jfire.jfire.core.beanfactory.BeanFactory;

import java.util.HashMap;
import java.util.Map;

@Resource(name = "allLoader")
public class AllLoader implements BeanFactory
{
    private final Map<Class, Object> holder = new HashMap<Class, Object>();

    public AllLoader()
    {
        holder.put(Person.class, new Person()
        {

            @Override
            public String getName()
            {
                return "name";
            }
        });
        holder.put(Home.class, new Home()
        {

            @Override
            public int getLength()
            {
                return 100;
            }
        });
    }

    @Override
    public <E> E getUnEnhanceyInstance(BeanDefinition beanDefinition)
    {
        return (E) holder.get(beanDefinition.getType());
    }
}
