package com.jfirer.jfire.test.function.beanannotest;

import com.jfirer.baseutil.Resource;

@Resource
public class NeedPerson5
{
    @Resource(name = "person5")
    private Person person;

    public Person getPerson()
    {
        return person;
    }
}
