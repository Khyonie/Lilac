package coffee.khyonieheart.lilac.parser.productions;

import java.util.Optional;

import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.parser.productions.value.ProductionArray;
import coffee.khyonieheart.lilac.parser.productions.value.ProductionBoolean;
import coffee.khyonieheart.lilac.parser.productions.value.ProductionFloat;
import coffee.khyonieheart.lilac.parser.productions.value.ProductionInlineTable;
import coffee.khyonieheart.lilac.parser.productions.value.ProductionInteger;
import coffee.khyonieheart.lilac.parser.productions.value.ProductionLocalDate;
import coffee.khyonieheart.lilac.parser.productions.value.ProductionLocalDateTime;
import coffee.khyonieheart.lilac.parser.productions.value.ProductionLocalTime;
import coffee.khyonieheart.lilac.parser.productions.value.ProductionOffsetDateTime;
import coffee.khyonieheart.lilac.parser.productions.value.ProductionString;
import coffee.khyonieheart.lilac.value.TomlObject;

public class ProductionValue 
{
	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser,
		String type
	)
		throws TomlSyntaxException
	{
		if (type != null)
		{
			Optional<TomlObject<?>> valueOption = ProductionInteger.parse(parser, type);
			if (valueOption.isPresent())
			{
				return valueOption;
			}

			valueOption = ProductionFloat.parse(parser, type);
			if (valueOption.isEmpty())
			{
				throw new TomlSyntaxException("Could not parse value with inline type \"" + type + "\"", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
			}

			return valueOption;
		}
		
		Optional<TomlObject<?>> valueOption = ProductionString.parse(parser);
		if (valueOption.isPresent())
		{
			return valueOption;
		}

		valueOption = ProductionOffsetDateTime.parse(parser);
		if (valueOption.isPresent())
		{
			return valueOption;
		}

		valueOption = ProductionLocalDateTime.parse(parser);
		if (valueOption.isPresent())
		{
			return valueOption;
		}

		valueOption = ProductionLocalTime.parse(parser);
		if (valueOption.isPresent())
		{
			return valueOption;
		}

		valueOption = ProductionLocalDate.parse(parser);
		if (valueOption.isPresent())
		{
			return valueOption;
		}

		valueOption = ProductionFloat.parse(parser, "float");
		if (valueOption.isPresent())
		{
			return valueOption;
		}

		valueOption = ProductionInteger.parse(parser, "integer");
		if (valueOption.isPresent())
		{
			return valueOption;
		}

		valueOption = ProductionBoolean.parse(parser);
		if (valueOption.isPresent())
		{
			return valueOption;
		}

		valueOption = ProductionInlineTable.parse(parser);
		if (valueOption.isPresent())
		{
			return valueOption;
		}

		valueOption = ProductionArray.parse(parser);

		return valueOption;
	}
}
