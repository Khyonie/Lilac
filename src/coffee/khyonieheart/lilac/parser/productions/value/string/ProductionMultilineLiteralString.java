package coffee.khyonieheart.lilac.parser.productions.value.string;

import java.util.Optional;

import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.parser.LilacDecoder;

public class ProductionMultilineLiteralString
{
	public static Optional<String> parse(
		LilacDecoder parser
	)
		throws TomlSyntaxException
	{
		String data = parser.getCurrentDocument().substring(parser.getPointer());

		if (data.length() < 6)
		{
			return Optional.empty();
		}

		if (!data.startsWith("'''"))
		{
			return Optional.empty();
		}

		int pointer = 3;
		parser.incrementPointer(3);

		if (data.charAt(pointer) == '\n')
		{
			parser.nextLine();
			pointer++;
			parser.incrementPointer(1);
		}

		StringBuilder builder = new StringBuilder();
		boolean foundEnd = false;
		while (pointer + 3 <= data.length())
		{
			if (data.substring(pointer, pointer + 3).equals("'''"))
			{
				foundEnd = true;
				break;
			}

			if (data.charAt(pointer) == '\n')
			{
				parser.nextLine();
			}

			builder.append(data.charAt(pointer));
			pointer++;
			parser.incrementPointer(1);
		}

		if (!foundEnd)
		{
			throw new TomlSyntaxException("Unterminated multiline literal string", parser.getLine(), parser.getLinePointer(), 3, parser.getCurrentDocument());
		}

		parser.incrementPointer(3);
		pointer += 3;
		if (pointer < data.length())
		{
			while (data.charAt(pointer) == '\'')
			{
				parser.incrementPointer(1);
				pointer++;
				builder.append("'");

				if (pointer >= data.length())
				{
					break;
				}
			}
		}

		return Optional.of(builder.toString());
	}
}
