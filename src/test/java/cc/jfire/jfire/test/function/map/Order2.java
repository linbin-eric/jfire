package cc.jfire.jfire.test.function.map;

import cc.jfire.baseutil.Resource;

@Resource
public class Order2 implements Order
{

    @Override
    public int getOrder()
    {
        return 2;
    }
}
