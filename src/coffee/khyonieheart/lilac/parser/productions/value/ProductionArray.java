package coffee.khyonieheart.lilac.parser.productions.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.api.Commentable;
import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.parser.productions.ProductionComment;
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
		TomlObject<?> previousValue = null;
		while (valueOption.isPresent())
		{
			data.add(valueOption.get());
			previousValue = valueOption.get();

			parser.toNextSymbol();
			Optional<String> commentOption = ProductionComment.parse(parser);
			if (commentOption.isPresent())
			{
				if (valueOption.get() instanceof Commentable c)
				{
					c.setComment(commentOption.get());
				}

				parser.toNextSymbol();
			}

			if (parser.parseLiteral(","))
			{	
				parser.toNextSymbol();

				valueOption = ProductionValue.parse(parser, null);

				parser.toNextSymbol();
				continue;
			} 

			break;
		}

		Optional<String> commentOption = ProductionComment.parse(parser);
		if (commentOption.isPresent())
		{
			if (previousValue instanceof Commentable c)
			{
				c.setComment(commentOption.get());
			}

			parser.toNextSymbol();
		}

		parser.toNextSymbol();

		if (!parser.parseLiteral("]"))
		{
			throw new TomlSyntaxException("Expected a \"]\" to end array", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		return Optional.of(new TomlArray(data));
	}
}
