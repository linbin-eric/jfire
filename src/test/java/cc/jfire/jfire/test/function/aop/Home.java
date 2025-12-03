package cc.jfire.jfire.test.function.aop;

import cc.jfire.baseutil.Resource;

@Resource
public class Home
{
    private String name = "home";

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
