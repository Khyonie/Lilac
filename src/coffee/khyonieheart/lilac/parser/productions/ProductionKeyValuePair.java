package coffee.khyonieheart.lilac.parser.productions;

import java.util.List;
import java.util.Optional;

import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.api.Commentable;
import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlObject;

public class ProductionKeyValuePair
{
	/**
	 * KeyValuePair:
	 * > Key [: JavaType] = Value [# Comment] {Newline}
	 */
	public static boolean parse(
		LilacDecoder parser
	) throws TomlSyntaxException
	{
		List<String> keys;
		Optional<List<String>> keyOption = ProductionKey.parse(parser);

		if (keyOption.isEmpty())
		{
			return false;
		}

		keys = keyOption.get();
		while (parser.consumeCharacters(' ', '\t'));

		// Attempt to consume inline Java type
		String javaType = null;
		if (parser.parseLiteral(":"))
		{
			Optional<String> typeOption = ProductionJavaType.parse(parser);
			
			if (typeOption.isEmpty())
			{
				throw new TomlSyntaxException("Expected a JavaType following \":\" symbol", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
			}

			javaType = typeOption.get();
			while (parser.consumeCharacters(' ', '\t'));
		}

		if (!parser.parseLiteral("="))
		{
			throw new TomlSyntaxException("Expected an \"=\" following key to complete KeyValuePair", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		while (parser.consumeCharacters(' ', '\t'));

		// Attempt to consume value. How values consume themselves is up to the specification of the value

		Optional<TomlObject<?>> valueOption = ProductionValue.parse(parser, javaType);

		if (valueOption.isEmpty())
		{
			throw new TomlSyntaxException("Could not parse value", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		TomlObject<?> value = valueOption.get();

		// Attempt to consume comment
		while (parser.consumeCharacters(' ', '\t'));

		Optional<String> commentOption = ProductionComment.parse(parser);
		if (commentOption.isPresent())
		{
			if (value instanceof Commentable c)
			{
				c.setComment(commentOption.get());
			}
		}

		// Consume newlines
		parser.toNextSymbol();

		parser.addKeyValuePair(keys, value);

		parser.addStep("Added a new key/value pair: " + keys + " of type " + value.getType().name());
		return true;
	}
}
