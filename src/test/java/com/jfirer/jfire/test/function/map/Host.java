package com.jfirer.jfire.test.function.map;

import com.jfirer.baseutil.Resource;
import com.jfirer.jfire.core.inject.notated.MapKeyMethodName;

import java.util.HashMap;
import java.util.Map;

@Resource
public class Host
{
    @Resource
    @MapKeyMethodName("getOrder")
    private final Map<Integer, Order> map  = new HashMap<Integer, Order>();
    @Resource
    private final Map<String, Order>  map2 = new HashMap<String, Order>();

    public Map<Integer, Order> getMap()
    {
        return map;
    }

    public Map<String, Order> getMap2()
    {
        return map2;
    }
}
