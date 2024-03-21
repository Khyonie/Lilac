package coffee.khyonieheart.lilac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import coffee.khyonieheart.lilac.api.NumberBase;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializeNumberAs
{
	public NumberBase value() default NumberBase.DECIMAL;
}
