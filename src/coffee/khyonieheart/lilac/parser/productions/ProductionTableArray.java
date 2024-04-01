package coffee.khyonieheart.lilac.parser.productions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.parser.LilacDecoder;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlTable;
import coffee.khyonieheart.lilac.value.TomlTableArray;

public class ProductionTableArray
{
	public static boolean parse(
		LilacDecoder parser
	)
		throws TomlSyntaxException
	{
		if (!parser.parseLiteral("[["))
		{
			return false;
		}

		while (parser.consumeCharacters(' ', '\t'));
		Optional<List<String>> keysOption = ProductionKey.parse(parser);

		if (keysOption.isEmpty())
		{
			throw new TomlSyntaxException("Expected at least one key for TableArray", parser.getLine(), parser.getLinePointer(), 1, parser.getCurrentDocument());
		}

		Map<String, TomlObject<?>> targetTable = parser.getTomlData();
		List<String> parents = new ArrayList<>();
		for (String key : keysOption.get())
		{
			parents.add(key);
			if (!targetTable.containsKey(key))
			{
				parser.addKeyValuePair(parents, new TomlTable(parents));
			}

			targetTable = switch (targetTable.get(key).getType())
			{
				case TABLE -> ((TomlTable) targetTable.get(key)).get();
				case INLINE_TABLE -> throw new TomlSyntaxException("Cannot modify inline table after creation", parser.getLine(), parser.getLinePointer(), key.length(), parser.getCurrentDocument());
				case TABLE_ARRAY -> throw new TomlSyntaxException("Cannot modify table array after creation", parser.getLine(), parser.getLinePointer(), key.length(), parser.getCurrentDocument());
				default -> throw new TomlSyntaxException("Table array cannot redefine key " + key, parser.getLine(), parser.getLinePointer(), key.length(), parser.getCurrentDocument());
			};
		}

		while (parser.consumeCharacters(' ', '\t'));

		if (!parser.parseLiteral("]]"))
		{
			throw new TomlSyntaxException("Expected double-bracket end \"]]\" to end array of tables", parser.getLine(), parser.getLinePointer(), 2, parser.getCurrentDocument());
		}

		while (parser.consumeCharacters(' ', '\t'));

		TomlTableArray tableArray = new TomlTableArray(keysOption.get());

		if (tableArray.equalsTable(parser.getCurrentTableArray()))
		{
			parser.getCurrentTableArray().startNextTable();
			return true;
		}

		parser.setCurrentTableArray(tableArray);
		return true;
	}
}
