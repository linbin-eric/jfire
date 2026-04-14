package cc.jfire.jfire.helpjunit;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestInstance(PER_METHOD)
@ExtendWith(JfireRunner.class)
public @interface JfireTest
{
}
