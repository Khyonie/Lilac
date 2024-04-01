package coffee.khyonieheart.lilac.parser.productions.value;

import java.util.Optional;

import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlDouble;
import coffee.khyonieheart.lilac.value.TomlFloat;
import coffee.khyonieheart.lilac.value.TomlObject;

public class ProductionFloat
{
	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser,
		String type
	) {
		Optional<String> floatOption = parser.parseRegex("([+-]?[\\d_]+\\.[\\d_]+(?:[eE][+-]?[\\d_]+)?)");

		if (floatOption.isPresent())
		{
			return Optional.of(switch (type) {
				case "float" -> new TomlFloat(Float.parseFloat(floatOption.get().replace("_", "")));
				case "double" -> new TomlDouble(Double.parseDouble(floatOption.get().replace("_", "")));
				default -> throw new IllegalStateException("Invalid float type \"" + type + "\"");
			});
		}

		floatOption = parser.parseRegex("([+-]?[\\d_]+[eE][+-]?[\\d_]+)");
		if (floatOption.isPresent())
		{
			return Optional.of(switch (type) {
				case "float" -> new TomlFloat(Float.parseFloat(floatOption.get().replace("_", "")));
				case "double" -> new TomlDouble(Double.parseDouble(floatOption.get().replace("_", "")));
				default -> throw new IllegalStateException("Invalid float type \"" + type + "\"");
			});
		}

		// Otherwise check special float types
		if (parser.parseLiteral("inf") || parser.parseLiteral("+inf"))
		{
			return Optional.of(switch (type) {
				case "float" -> new TomlFloat(Float.POSITIVE_INFINITY);
				case "double" -> new TomlDouble(Double.POSITIVE_INFINITY);
				default -> throw new IllegalStateException("Invalid float type \"" + type + "\"");
			});
		}

		if (parser.parseLiteral("-inf"))
		{
			return Optional.of(switch (type) {
				case "float" -> new TomlFloat(Float.NEGATIVE_INFINITY);
				case "double" -> new TomlDouble(Double.NEGATIVE_INFINITY);
				default -> throw new IllegalStateException("Invalid float type \"" + type + "\"");
			});
		}

		if (parser.parseLiteral("nan") || parser.parseLiteral("+nan"))
		{
			return Optional.of(switch (type) {
				case "float" -> new TomlFloat(Float.NaN);
				case "double" -> new TomlDouble(Double.NaN);
				default -> throw new IllegalStateException("Invalid float type \"" + type + "\"");
			});
		}

		if (parser.parseLiteral("-nan"))
		{
			System.out.println("Warning: Java does not support the \"negative not-a-number\" float/double type. It has been converted to NaN.");
			return Optional.of(switch (type) {
				case "float" -> new TomlFloat(Float.NaN);
				case "double" -> new TomlDouble(Double.NaN);
				default -> throw new IllegalStateException("Invalid float type \"" + type + "\"");
			});
		}

		return Optional.empty();
	}
}
