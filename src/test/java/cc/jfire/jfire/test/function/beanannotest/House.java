package cc.jfire.jfire.test.function.beanannotest;

import cc.jfire.baseutil.Resource;

@Resource(name = "house")
public class House
{
    private String name = "2";

    public String name()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
