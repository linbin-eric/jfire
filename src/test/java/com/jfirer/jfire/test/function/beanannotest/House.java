package com.jfirer.jfire.test.function.beanannotest;

import com.jfirer.baseutil.Resource;

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
