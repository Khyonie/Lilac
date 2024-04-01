package coffee.khyonieheart.lilac.parser.productions.value;

import java.time.LocalDate;
import java.util.Optional;

import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlLocalDate;
import coffee.khyonieheart.lilac.value.TomlObject;

public class ProductionLocalDate
{
	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser
	) {
		Optional<String> valueString = parser.parseRegex("(\\d{4}-\\d{2}-\\d{2})");

		if (valueString.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(new TomlLocalDate(LocalDate.parse(valueString.get())));
	}
}
