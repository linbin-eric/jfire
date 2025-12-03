package cc.jfire.jfire.test.function.map;

import cc.jfire.baseutil.Resource;

@Resource
public class Order1 implements Order
{
    @Override
    public int getOrder()
    {
        return 1;
    }
}
