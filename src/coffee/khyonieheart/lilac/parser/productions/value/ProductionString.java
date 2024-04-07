package coffee.khyonieheart.lilac.parser.productions.value;

import java.util.Optional;

import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.api.StringType;
import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlString;

public class ProductionString
{
	private static final String LEGAL_CHARS = "[^\u0000-\u0008\n-\u001F\u007F]"; // Regex that matches all but illegal chars

	//private static final String MULTILINE_BASIC_REGEX = "\"\"\"((?:\\\"|\n|.)*?)\"\"\"";
	private static final String MULTILINE_BASIC_REGEX = "\"\"\"((?:\\\"|.|\n)*?)\"\"\"";
	private static final String BASIC_REGEX = "\"((?:\\\"|" + LEGAL_CHARS + ")*)\"";
	private static final String MULTILINE_LITERAL_REGEX = "'''(?:\\s*)?((?:'|\n|" + LEGAL_CHARS + ")*)'''";
	private static final String LITERAL_REGEX = "'(" + LEGAL_CHARS + "*?)'";

	private static final String LINE_ENDING_BACKSLASH_REGEX = "\\\n\\s*";

	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser
	)
		throws TomlSyntaxException
	{
		/*
		Optional<String> stringOption = parser.parseRegex(MULTILINE_BASIC_REGEX);
		if (stringOption.isPresent())
		{
			for (char c : stringOption.get().toCharArray())
			{
				if (c == '\n')
				{
					parser.nextLine();
				}
			}

			String finalString = stringOption.get().replaceAll(LINE_ENDING_BACKSLASH_REGEX, "");
			return Optional.of(new TomlString(finalString, StringType.MULTILINE_BASIC));
		}
		*/
		Optional<String> stringOption = consumeString(parser, "\"\"\"");
		if (stringOption.isPresent())
		{
			return Optional.of(new TomlString(stringOption.get(), StringType.MULTILINE_BASIC));
		}

		//stringOption = parser.parseRegex("\"((?:\\\"|.)*?)\"");
		stringOption = parser.parseRegex(BASIC_REGEX);
		if (stringOption.isPresent())
		{
			String finalString = stringOption.get().replaceAll(LINE_ENDING_BACKSLASH_REGEX, "");
			return Optional.of(new TomlString(finalString, StringType.BASIC));
		}

		stringOption = parser.parseRegex(MULTILINE_LITERAL_REGEX);
		if (stringOption.isPresent())
		{
			for (char c : stringOption.get().toCharArray())
			{
				if (c == '\n')
				{
					parser.nextLine();
				}
			}
			return Optional.of(new TomlString(stringOption.get(), StringType.MULTILINE_LITERAL));
		}

		stringOption = parser.parseRegex(LITERAL_REGEX);
		if (stringOption.isPresent())
		{
			return Optional.of(new TomlString(stringOption.get(), StringType.LITERAL));
		}

		return Optional.empty();
	}

	private static Optional<String> consumeString(
		LilacDecoder parser,
		String stringCaps
	)
		throws TomlSyntaxException
	{
		if (!parser.parseLiteral(stringCaps))
		{
			return Optional.empty();
		}

		StringBuilder builder = new StringBuilder();

		while (parser.getPointer() + stringCaps.length() < parser.getCurrentDocument().length())
		{
			if (parser.getCurrentDocument().substring(parser.getPointer(), parser.getPointer() + stringCaps.length()).equals(stringCaps))
			{
				return Optional.of(builder.toString());
			}

			char currentChar = parser.charAtPointer();
			if (currentChar == '\n')
			{
				builder.append('\n');
				parser.incrementPointer(1);
				continue;
			}

			if (currentChar == '\\')
			{
				parser.incrementPointer(1);
				if (parser.charAtPointer() == '\n')
				{
					parser.nextLine();
					while (Character.isWhitespace(parser.charAtPointer()))
					{
						if (parser.charAtPointer() == '\n')
						{
							parser.nextLine();
						}
						parser.incrementPointer(1);
					}

					continue;
				}

				builder.append('\\');
				continue;
			}

			if ((currentChar >= '\u0000' && currentChar <= '\u0008') ||
				(currentChar > '\n' && currentChar <= '\u001F') ||
				(currentChar == '\u007F')) 
			{
				throw new TomlSyntaxException("Illegal character in string", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
			}

			builder.append(parser.charAtPointer());
			parser.incrementPointer(1);
		}

		return Optional.empty();
	}
}
