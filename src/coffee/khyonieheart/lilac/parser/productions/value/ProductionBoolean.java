package coffee.khyonieheart.lilac.parser.productions.value;

import java.util.Optional;

import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlBoolean;
import coffee.khyonieheart.lilac.value.TomlObject;

public class ProductionBoolean
{
	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser
	) {
		if (parser.parseLiteral("true"))
		{
			return Optional.of(new TomlBoolean(true));
		}

		if (parser.parseLiteral("false"))
		{
			return Optional.of(new TomlBoolean(false));
		}

		return Optional.empty();
	} 
}
