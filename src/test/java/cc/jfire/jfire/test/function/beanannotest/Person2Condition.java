package cc.jfire.jfire.test.function.beanannotest;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.condition.Condition;
import cc.jfire.jfire.core.prepare.annotation.condition.ErrorMessage;

import java.lang.reflect.AnnotatedElement;

public class Person2Condition implements Condition
{
    @Override
    public boolean match(ApplicationContext context, AnnotatedElement element, ErrorMessage errorMessage)
    {
        if ("pass".equals(context.getConfig().fullPathConfig().get("person2")))
        {
            return true;
        }
        else
        {
            errorMessage.addErrorMessage("环境中不包含属性person2");
            return false;
        }
    }
}
