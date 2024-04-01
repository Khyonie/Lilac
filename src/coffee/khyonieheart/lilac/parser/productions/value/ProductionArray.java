package coffee.khyonieheart.lilac.parser.productions.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.parser.productions.ProductionValue;
import coffee.khyonieheart.lilac.value.TomlArray;
import coffee.khyonieheart.lilac.value.TomlObject;

public class ProductionArray
{
	/**
	 * [ {Value [,]} [,] ]
	 */
	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser
	)
		throws TomlSyntaxException
	{
		if (!parser.parseLiteral("["))
		{
			return Optional.empty();
		}

		parser.toNextSymbol();

		List<TomlObject<?>> data = new ArrayList<>();
		Optional<TomlObject<?>> valueOption = ProductionValue.parse(parser, null);
		while (valueOption.isPresent())
		{
			data.add(valueOption.get());

			parser.toNextSymbol();

			if (parser.parseLiteral(","))
			{	
				parser.toNextSymbol();

				valueOption = ProductionValue.parse(parser, null);

				parser.toNextSymbol();
				continue;
			} 

			break;
		}

		parser.toNextSymbol();

		if (!parser.parseLiteral("]"))
		{
			throw new TomlSyntaxException("Expected a \"]\" to end array", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		return Optional.of(new TomlArray(data));
	}
}
