package coffee.khyonieheart.lilac.adapter.builtins;

import coffee.khyonieheart.lilac.value.TomlBoolean;
import coffee.khyonieheart.lilac.value.TomlByte;
import coffee.khyonieheart.lilac.value.TomlDouble;
import coffee.khyonieheart.lilac.value.TomlFloat;
import coffee.khyonieheart.lilac.value.TomlInteger;
import coffee.khyonieheart.lilac.value.TomlLong;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlShort;

public class PrimitiveTomlAdapter
{
	public static TomlObject<?> toToml(
		Object object
	) {
		return switch (object.getClass().getSimpleName())
		{
			case "byte" -> new TomlByte((byte) object);
			case "short" -> new TomlShort((short) object);
			case "int" -> new TomlInteger((int) object);
			case "long" -> new TomlLong((long) object);
			case "float" -> new TomlFloat((float) object);
			case "double" -> new TomlDouble((double) object);
			case "boolean" -> new TomlBoolean((boolean) object);
			case "char" -> throw new UnsupportedOperationException("TOML does not support character literals");
			default -> throw new IllegalArgumentException("Input object must be a primitive");
		};
	}
}
