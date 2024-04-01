package coffee.khyonieheart.lilac.parser.productions;

import java.util.Optional;

import coffee.khyonieheart.lilac.parser.LilacDecoder;

public class ProductionJavaType
{
	public static Optional<String> parse(
		LilacDecoder parser
	) {
		parser.addStep("Parsing JavaType");
		if (parser.parseLiteral("byte"))
		{
			return Optional.of("byte");
		}

		if (parser.parseLiteral("short"))
		{
			return Optional.of("short");
		}

		if (parser.parseLiteral("integer"))
		{
			return Optional.of("integer");
		}

		if (parser.parseLiteral("long"))
		{
			return Optional.of("long");
		}

		if (parser.parseLiteral("float"))
		{
			return Optional.of("float");
		}

		if (parser.parseLiteral("double"))
		{
			return Optional.of("double");
		}

		return Optional.empty();
	}
}
