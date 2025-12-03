package cc.jfire.jfire.core.prepare.annotation.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用该注解，意味着该类是一个配置类。用于以下几个场景：
 * 1、配置类上可以使用 @AddProperty 注解，用于添加配置信息
 * 2、配置类上可以使用 @PropertyPath 注解，用于添加配置文件路径
 * 3、配置类上可以使用 @ProfileSelector 注解，用于添加根据环境选择的配置文件路径
 * 4、配置类的方法可以使用@Bean 注解，用于将方法的返回结果添加为 Bean。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration
{

}
