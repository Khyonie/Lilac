package coffee.khyonieheart.lilac.parser.productions.value;

import java.time.OffsetDateTime;
import java.util.Optional;

import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlOffsetDateTime;

public class ProductionOffsetDateTime
{
	private static final String PATTERN = "(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?(?:Z|[+-]\\d{2}:\\d{2}))";

	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser
	) {
		Optional<String> valueString = parser.parseRegex(PATTERN);

		if (valueString.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(new TomlOffsetDateTime(OffsetDateTime.parse(valueString.get())));
	}
}
