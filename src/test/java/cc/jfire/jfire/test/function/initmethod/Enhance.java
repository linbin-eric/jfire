package cc.jfire.jfire.test.function.initmethod;

import cc.jfire.baseutil.Resource;
import cc.jfire.jfire.core.aop.ProceedPoint;
import cc.jfire.jfire.core.aop.notated.Before;
import cc.jfire.jfire.core.aop.notated.EnhanceClass;


@Resource
@EnhanceClass("com.jfire.*.start*")
public class Enhance
{
    @Before("initage()")
    public void initage(ProceedPoint point)
    {
        System.out.println("dads");
    }
}
