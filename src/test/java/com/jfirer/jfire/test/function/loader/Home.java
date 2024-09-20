package com.jfirer.jfire.test.function.loader;

import com.jfirer.baseutil.Resource;
import com.jfirer.jfire.core.beanfactory.SelectBeanFactory;


@Resource
@SelectBeanFactory("allLoader")
public interface Home
{
    int getLength();
}
