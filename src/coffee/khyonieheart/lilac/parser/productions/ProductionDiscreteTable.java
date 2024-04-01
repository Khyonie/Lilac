package coffee.khyonieheart.lilac.parser.productions;

import java.util.List;
import java.util.Optional;

import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlTable;

public class ProductionDiscreteTable
{
	/**
	 * [ Key {. Key} ] [Comment] {Newline}
	 */
	public static Optional<TomlTable> parse(
		LilacDecoder parser
	)
		throws TomlSyntaxException
	{
		if (!parser.parseLiteral("["))
		{
			return Optional.empty();
		}

		Optional<List<String>> keys = ProductionKey.parse(parser);

		if (keys.isEmpty())
		{
			throw new TomlSyntaxException("A table must explicitly define at least one key", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		while (parser.consumeCharacters(' ', '\t'));

		if (!parser.parseLiteral("]"))
		{
			throw new TomlSyntaxException("Expected a \"]\" to end DiscreteTable", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		List<String> parents = keys.get().subList(0, keys.get().size() - 1);
		String key = keys.get().get(keys.get().size() - 1);

		TomlTable table = new TomlTable(key, parents);

		while (parser.consumeCharacters(' ', '\t'));
		Optional<String> comment = ProductionComment.parse(parser);
		if (comment.isPresent())
		{
			table.setComment(comment.get());
			while (parser.consumeCharacters(' ', '\t'));
		}

		while (parser.consumeCharacters('\n'))
		{
			table.incrementTrailingNewlines();
			parser.nextLine();
			while (parser.consumeCharacters(' ', '\t'));
		}

		parser.clearCurrentTableArray();

		return Optional.of(table);
	}
}
