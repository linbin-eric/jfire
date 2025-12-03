package cc.jfire.jfire.core.aop.notated.cache;

import java.lang.annotation.*;

/**
 * 缓存删除注解，用于删除缓存中的数据
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CacheDelete
{
    /**
     * key的规则
     *
     * @return 缓存key表达式
     */
    String value();

    /**
     * 缓存名称
     *
     * @return 缓存名称
     */
    String cacheName() default "default";

    /**
     * 进行缓存操作的条件
     *
     * @return 条件表达式
     */
    String condition() default "";
}
