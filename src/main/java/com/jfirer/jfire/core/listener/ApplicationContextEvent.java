package com.jfirer.jfire.core.listener;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;
import com.jfirer.jfire.core.prepare.ContextPrepare;

public interface ApplicationContextEvent
{
    public static class RefreshStart implements ApplicationContextEvent
    {}

    class RefreshEnd implements ApplicationContextEvent
    {}

    class BeanRegister implements ApplicationContextEvent
    {
        private BeanRegisterInfo beanRegisterInfo;

        public BeanRegister(BeanRegisterInfo beanRegisterInfo)
        {
            this.beanRegisterInfo = beanRegisterInfo;
        }

        public BeanRegisterInfo getBeanRegisterInfo()
        {
            return beanRegisterInfo;
        }
    }

    class ExecuteContextPrepare implements ApplicationContextEvent
    {
        ContextPrepare contextPrepare;
        int            cost;

        public ExecuteContextPrepare(ContextPrepare contextPrepare, int cost)
        {
            this.contextPrepare = contextPrepare;
            this.cost = cost;
        }

        public ContextPrepare getContextPrepare()
        {
            return contextPrepare;
        }

        public int getCost()
        {
            return cost;
        }
    }

    class ExecuteEnhanceManager implements ApplicationContextEvent
    {
        EnhanceManager enhanceManager;
        int            cost;

        public ExecuteEnhanceManager(EnhanceManager enhanceManager, int cost)
        {
            this.enhanceManager = enhanceManager;
            this.cost = cost;
        }

        public EnhanceManager getEnhanceManager()
        {
            return enhanceManager;
        }

        public int getCost()
        {
            return cost;
        }
    }

    class ExecuteAwareContextInit implements ApplicationContextEvent
    {
        BeanRegisterInfo beanRegisterInfo;
        int              cost;

        public ExecuteAwareContextInit(BeanRegisterInfo beanRegisterInfo, int cost)
        {
            this.beanRegisterInfo = beanRegisterInfo;
            this.cost = cost;
        }

        public BeanRegisterInfo getBeanRegisterInfo()
        {
            return beanRegisterInfo;
        }

        public int getCost()
        {
            return cost;
        }
    }

    class BeanBuildInstance implements ApplicationContextEvent
    {
        String   beanName;
        Class<?> type;
        int      buildCost;
        int      initCost;

        public BeanBuildInstance(String beanName, Class<?> type, int buildCost, int initCost)
        {
            this.beanName = beanName;
            this.type = type;
            this.buildCost = buildCost;
            this.initCost = initCost;
        }

        public String getBeanName()
        {
            return beanName;
        }

        public Class<?> getType()
        {
            return type;
        }

        public int getBuildCost()
        {
            return buildCost;
        }

        public int getInitCost()
        {
            return initCost;
        }
    }
}
