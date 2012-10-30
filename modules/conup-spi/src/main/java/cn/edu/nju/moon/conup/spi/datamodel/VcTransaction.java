package cn.edu.nju.moon.conup.spi.datamodel;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface VcTransaction {
	String name() default "";
	
	String[] states() default { "" };

	String[] next() default { "" };

}
