package coffee.khyonieheart.lilac.parser.productions.value;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.parser.productions.ProductionJavaType;
import coffee.khyonieheart.lilac.parser.productions.ProductionKey;
import coffee.khyonieheart.lilac.parser.productions.ProductionValue;
import coffee.khyonieheart.lilac.value.TomlInlineTable;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlObjectType;
import coffee.khyonieheart.lilac.value.TomlTable;

public class ProductionInlineTable
{
	/**
	 * { [key [: JavaType] = Value {, key [: JavaType] = Value}] }
	 */
	public static Optional<TomlObject<?>> parse(
		LilacDecoder parser
	)
		throws TomlSyntaxException
	{
		if (!parser.parseLiteral("{"))
		{
			return Optional.empty();
		}

		while (parser.consumeCharacters(' ', '\t'));

		Map<String, TomlObject<?>> data = new LinkedHashMap<>();

		if (parser.parseLiteral("}"))
		{
			return Optional.of(new TomlInlineTable(data));
		}

		parseKeyValuePair(parser, data);
		while (parser.consumeCharacters(' ', '\t'));

		while (parser.parseLiteral(","))
		{
			while (parser.consumeCharacters(' ', '\t'));
			parseKeyValuePair(parser, data);
			while (parser.consumeCharacters(' ', '\t'));
		}

		while (parser.consumeCharacters(' ', '\t'));

		if (!parser.parseLiteral("}"))
		{
			throw new TomlSyntaxException("Expected inline table end \"}\"", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		return Optional.of(new TomlInlineTable(data));
	}

	private static void parseKeyValuePair(
		LilacDecoder parser,
		Map<String, TomlObject<?>> data
	)
		throws TomlSyntaxException
	{
		Optional<List<String>> keyOption = ProductionKey.parse(parser);

		if (keyOption.isEmpty())
		{
			throw new TomlSyntaxException("Expected a key for inline table", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		while (parser.consumeCharacters(' ', '\t'));

		String type = null;
		if (parser.parseLiteral(":"))
		{
			while (parser.consumeCharacters(' ', '\t'));
			
			Optional<String> typeOption = ProductionJavaType.parse(parser);

			if (typeOption.isEmpty())
			{
				throw new TomlSyntaxException("Expected a JavaType after inline type declaration \":\"", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
			}

			type = typeOption.get();
			while (parser.consumeCharacters(' ', '\t'));
		}

		if (!parser.parseLiteral("="))
		{
			throw new TomlSyntaxException("Expected a \"=\" following key to complete inline KeyValuePair", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		while (parser.consumeCharacters(' ', '\t'));

		Optional<TomlObject<?>> valueOption = ProductionValue.parse(parser, type);
		if (valueOption.isEmpty())
		{
			throw new TomlSyntaxException("Could not parse value inside inline table", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		List<String> parents = keyOption.get().subList(0, keyOption.get().size() - 1);
		String key = keyOption.get().get(keyOption.get().size() - 1);

		List<String> parentList = new ArrayList<>();
		Map<String, TomlObject<?>> targetTable = data;
		for (String parent : parents)
		{
			if (!targetTable.containsKey(parent))
			{
				targetTable.put(parent, new TomlTable(new ArrayList<>(parentList)));
			}

			if (targetTable.get(parent).getType() != TomlObjectType.TABLE)
			{
				throw new TomlSyntaxException("Cannot redefine inline table key " + parent, parser.getLine(), parser.getLinePointer(), parent.length(), parser.getCurrentDocument());
			}

			parentList.add(parent);
			targetTable = switch (targetTable.get(parent).getType()) {
				case TABLE -> ((TomlTable) targetTable.get(parent)).get();
				case INLINE_TABLE -> throw new TomlSyntaxException("Cannot modify inline table after creation", parser.getLine(), parser.getLinePointer(), key.length(), parser.getCurrentDocument());
				default -> throw new TomlSyntaxException("Cannot redefine existing key " + parent + " with type " + targetTable.get(parent).getType().name() + " as a table", parser.getLine(), parser.getLinePointer(), parent.length(), parser.getCurrentDocument());
			};
		}

		if (targetTable.containsKey(key))
		{
			throw new TomlSyntaxException("Cannot redefine existing key " + key, parser.getLine(), parser.getLinePointer(), key.length(), parser.getCurrentDocument());
		}

		targetTable.put(key, valueOption.get());
	}
}
