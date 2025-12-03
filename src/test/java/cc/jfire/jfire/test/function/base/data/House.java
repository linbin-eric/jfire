package cc.jfire.jfire.test.function.base.data;

import cc.jfire.baseutil.Resource;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.AwareContextInited;


@Resource
public class House implements AwareContextInited
{
    private String          name;
    @Resource
    private ImmutablePerson host;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ImmutablePerson getHost()
    {
        return host;
    }

    public void setHost(ImmutablePerson host)
    {
        this.host = host;
    }

    @Override
    public void aware(ApplicationContext environment)
    {
        name = "林斌的房子";
    }
}
