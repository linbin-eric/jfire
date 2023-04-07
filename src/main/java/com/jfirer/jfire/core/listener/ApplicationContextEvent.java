package com.jfirer.jfire.core.listener;

import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import lombok.Builder;
import lombok.Data;

public interface ApplicationContextEvent
{
    class RefreshStart implements ApplicationContextEvent
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
        long           cost;

        public ExecuteContextPrepare(ContextPrepare contextPrepare, long cost)
        {
            this.contextPrepare = contextPrepare;
            this.cost = cost;
        }

        public ContextPrepare getContextPrepare()
        {
            return contextPrepare;
        }

        public long getCost()
        {
            return cost;
        }
    }

    class ExecuteEnhanceManager implements ApplicationContextEvent
    {
        EnhanceManager enhanceManager;
        long           cost;

        public ExecuteEnhanceManager(EnhanceManager enhanceManager, long cost)
        {
            this.enhanceManager = enhanceManager;
            this.cost = cost;
        }

        public EnhanceManager getEnhanceManager()
        {
            return enhanceManager;
        }

        public long getCost()
        {
            return cost;
        }
    }

    class ExecuteAwareContextInit implements ApplicationContextEvent
    {
        BeanRegisterInfo beanRegisterInfo;
        long             cost;

        public ExecuteAwareContextInit(BeanRegisterInfo beanRegisterInfo, long cost)
        {
            this.beanRegisterInfo = beanRegisterInfo;
            this.cost = cost;
        }

        public BeanRegisterInfo getBeanRegisterInfo()
        {
            return beanRegisterInfo;
        }

        public long getCost()
        {
            return cost;
        }
    }

    @Data
    @Builder
    class BeanBuildInstance implements ApplicationContextEvent
    {
        String   beanName;
        Class<?> type;
        long     cycTestCost;
        long     getUnEnhanceInstanceCost;
        long     enhanceCost;
        long     injectCost;
        long     setEnhanceFieldsCost;
        long     postConstructMethodCost;
        long     allCost;
    }
}
