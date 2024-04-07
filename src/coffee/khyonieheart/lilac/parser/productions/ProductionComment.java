package coffee.khyonieheart.lilac.parser.productions;

import java.util.Optional;

import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.parser.LilacDecoder;

public class ProductionComment
{
	public static Optional<String> parse(
		LilacDecoder parser
	)
		throws TomlSyntaxException
	{
		if (!parser.parseLiteral("#"))
		{
			return Optional.empty();
		}

		StringBuilder commentBuilder = new StringBuilder();
		while (parser.charAtPointer() != '\n')
		{
			char pointerChar = parser.charAtPointer();

			if ((pointerChar >= 0x0000 && pointerChar <= 0x0008) ||
				(pointerChar >= 0x000A && pointerChar <= 0x001F) || 
				pointerChar == 0x007F)
			{
				throw new TomlSyntaxException("Illegal character in comment", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
			}

			commentBuilder.append(pointerChar);
			parser.incrementPointer(1);

			if (parser.getPointer() == parser.getCurrentDocument().length())
			{
				break;
			}
		}

		return Optional.of(commentBuilder.toString());
	}
}
