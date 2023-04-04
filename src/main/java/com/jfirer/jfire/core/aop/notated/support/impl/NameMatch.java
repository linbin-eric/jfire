package com.jfirer.jfire.core.aop.notated.support.impl;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.jfire.core.aop.notated.support.MatchTargetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class NameMatch implements MatchTargetMethod
{
    private final        String rule;
    private static final Logger logger = LoggerFactory.getLogger(NameMatch.class);

    public NameMatch(String rule)
    {
        this.rule = rule;
    }

    @Override
    public boolean match(Method method)
    {
        logger.trace("准备匹配AOP方法拦截，规则:{},方法:{}",  rule, method.toGenericString());
        String methodNameRule = rule.substring(0, rule.indexOf('('));
        if (!StringUtil.match(method.getName(), methodNameRule))
        {
            return false;
        }
        String paramRule = rule.substring(rule.indexOf('(') + 1, rule.length() - 1);
        if ("*".equals(paramRule))
        {
            return true;
        }
        if ("".equals(paramRule))
        {
            return method.getParameterTypes().length == 0;
        }
        String[]   split          = paramRule.split(",");
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (split.length != parameterTypes.length)
        {
            return false;
        }
        for (int i = 0; i < split.length; i++)
        {
            String literals = split[i].trim();
            if ("*".equals(literals))
            {
                continue;
            }
            if (!literals.equals(parameterTypes[i].getSimpleName()) && !literals.equals(parameterTypes[i].getName()))
            {
                return false;
            }
        }
        return true;
    }
}
