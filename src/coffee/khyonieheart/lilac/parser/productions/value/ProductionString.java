package coffee.khyonieheart.lilac.parser.productions.value;

import java.util.Optional;

import coffee.khyonieheart.lilac.api.StringType;
import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlString;

public class ProductionString
{
	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser
	) {
		Optional<String> stringOption = parser.parseRegex("\"\"\"((?:\\\"|\n|.)*?)\"\"\"");
		if (stringOption.isPresent())
		{
			for (char c : stringOption.get().toCharArray())
			{
				if (c == '\n')
				{
					parser.nextLine();
				}
			}
			return Optional.of(new TomlString(stringOption.get(), StringType.MULTILINE_BASIC));
		}

		stringOption = parser.parseRegex("\"((?:\\\"|.)*)\"");
		if (stringOption.isPresent())
		{
			return Optional.of(new TomlString(stringOption.get(), StringType.BASIC));
		}

		stringOption = parser.parseRegex("'''\n*((?:\\'|\n|.)*?)'''");
		if (stringOption.isPresent())
		{
			return Optional.of(new TomlString(stringOption.get(), StringType.MULTILINE_LITERAL));
		}

		stringOption = parser.parseRegex("'((?:\\'|.)*?)'");
		if (stringOption.isPresent())
		{
			return Optional.of(new TomlString(stringOption.get(), StringType.LITERAL));
		}

		return Optional.empty();
	}
}
