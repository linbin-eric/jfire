package cc.jfire.jfire.test.function.beanannotest;

import cc.jfire.baseutil.Resource;

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
