package coffee.khyonieheart.lilac.parser.productions.value;

import java.time.LocalTime;
import java.util.Optional;

import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlLocalTime;
import coffee.khyonieheart.lilac.value.TomlObject;

public class ProductionLocalTime
{
	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser
	) {
		Optional<String> valueString = parser.parseRegex("(\\d{2}:\\d{2}\\d{2}.\\d+)");

		if (valueString.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(new TomlLocalTime(LocalTime.parse(valueString.get())));
	}
}
