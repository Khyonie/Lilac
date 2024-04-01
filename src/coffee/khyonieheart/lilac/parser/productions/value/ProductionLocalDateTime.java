package coffee.khyonieheart.lilac.parser.productions.value;

import java.time.LocalDateTime;
import java.util.Optional;

import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlLocalDateTime;
import coffee.khyonieheart.lilac.value.TomlObject;

public class ProductionLocalDateTime
{
	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser
	) {
		Optional<String> valueString = parser.parseRegex("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+)");

		if (valueString.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(new TomlLocalDateTime(LocalDateTime.parse(valueString.get())));
	}
}
