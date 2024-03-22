package coffee.khyonieheart.lilac.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LilacExpose
{
	public boolean serialize() default true;
	public boolean deserialize() default true;
}
