package coffee.khyonieheart.lilac.parser.productions.value;

import java.util.Optional;

import coffee.khyonieheart.lilac.api.NumberBase;
import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlByte;
import coffee.khyonieheart.lilac.value.TomlInteger;
import coffee.khyonieheart.lilac.value.TomlLong;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlShort;

public class ProductionInteger
{
	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser,
		String type
	) {
		NumberBase base;
		String value;

		// Hexadecimal
		Optional<String> stringOption = parser.parseRegex("0[xX]((?:_|[0-9a-fA-F])+)");
		if (stringOption.isPresent())
		{
			value = stringOption.get().substring(2).replace("_", "");
			base = NumberBase.HEXADECIMAL;

			return Optional.of(toValue(value, type, base));
		}

		// Octal
		stringOption = parser.parseRegex("0o((?:_|[0-7])+)");
		if (stringOption.isPresent())
		{
			value = stringOption.get().substring(2).replace("_", "");
			base = NumberBase.OCTAL;

			return Optional.of(toValue(value, type, base));
		}

		// Binary
		stringOption = parser.parseRegex("0[bB]((?:_|[01])+)");
		if (stringOption.isPresent())
		{
			value = stringOption.get().substring(2).replace("_", "");
			base = NumberBase.BINARY;

			return Optional.of(toValue(value, type, base));
		}

		// Decimal
		stringOption = parser.parseRegex("([+-]?(?:_|\\d)+)");
		//stringOption = parser.parseRegex("([+-]?[1-9]+(?:_\\d+)*)");
		if (stringOption.isPresent())
		{
			value = stringOption.get().replace("_", "");
			base = NumberBase.DECIMAL;

			return Optional.of(toValue(value, type, base));
		}

		// Literally just zero
		stringOption = parser.parseRegex("([+-]?0)");
		if (stringOption.isPresent())
		{
			return Optional.of(toValue("0", type, NumberBase.DECIMAL));
		}

		return Optional.empty();
	}

	private static TomlObject<?> toValue(
		String value,
		String type,
		NumberBase base
	) {
		return switch (type)
		{
			case "byte" -> new TomlByte((byte) Short.parseShort(value, base.getRadix()), base);
			case "short" -> new TomlShort((short) Integer.parseInt(value, base.getRadix()), base);
			case "integer" -> new TomlInteger(Integer.parseInt(value, base.getRadix()), base);
			case "long" -> new TomlLong(Long.parseLong(value, base.getRadix()), base);
			default -> throw new IllegalStateException("Invalid integer type \"" + type + "\"");
		};
	}
}
