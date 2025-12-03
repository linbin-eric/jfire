package cc.jfire.jfire.test.function.loader;

import cc.jfire.baseutil.Resource;
import cc.jfire.jfire.core.beanfactory.SelectBeanFactory;

@Resource
@SelectBeanFactory("allLoader")
public interface Person
{
    String getName();
}
