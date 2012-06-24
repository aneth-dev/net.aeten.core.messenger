package net.aeten.core.args4j;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ValueType {
	Class<?> value();
}
