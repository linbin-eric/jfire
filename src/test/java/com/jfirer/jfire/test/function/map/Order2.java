package com.jfirer.jfire.test.function.map;

import com.jfirer.baseutil.Resource;

@Resource
public class Order2 implements Order
{

    @Override
    public int getOrder()
    {
        return 2;
    }
}
