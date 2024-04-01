package coffee.khyonieheart.lilac.parser.productions;

import java.util.Optional;

import coffee.khyonieheart.lilac.parser.LilacDecoder;

public class ProductionComment
{
	public static Optional<String> parse(
		LilacDecoder parser
	) {
		if (!parser.parseLiteral("#"))
		{
			return Optional.empty();
		}

		StringBuilder commentBuilder = new StringBuilder();
		while (parser.charAtPointer() != '\n')
		{
			commentBuilder.append(parser.charAtPointer());
			parser.incrementPointer(1);

			if (parser.getPointer() == parser.getCurrentDocument().length())
			{
				break;
			}
		}

		parser.incrementPointer(1);

		return Optional.of(commentBuilder.toString());
	}
}
