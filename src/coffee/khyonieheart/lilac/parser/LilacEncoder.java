package coffee.khyonieheart.lilac.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import coffee.khyonieheart.lilac.TomlConfiguration;
import coffee.khyonieheart.lilac.TomlEncoder;
import coffee.khyonieheart.lilac.TomlParser;
import coffee.khyonieheart.lilac.api.Commentable;
import coffee.khyonieheart.lilac.value.TomlArray;
import coffee.khyonieheart.lilac.value.TomlByte;
import coffee.khyonieheart.lilac.value.TomlInteger;
import coffee.khyonieheart.lilac.value.TomlLong;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlObjectType;
import coffee.khyonieheart.lilac.value.TomlShort;
import coffee.khyonieheart.lilac.value.TomlTable;

public class LilacEncoder implements TomlEncoder
{
	private static Pattern quotedKeyPattern = Pattern.compile("[^A-Za-z0-9\\_-]");

	@Override
	public String encode(
		TomlConfiguration configuration,
		TomlParser parser
	) {
		StringBuilder builder = new StringBuilder();

		if (configuration.getBacking().isEmpty())
		{
			return "";
		}

		encodeTable(configuration.getBacking(), new ArrayList<>(), builder, parser);

		// Trim
		while (builder.charAt(builder.length() - 1) == '\n')
		{
			builder.deleteCharAt(builder.length() - 1);
		}

		return builder.toString();
	}

	private void encodeTable(
		Map<String, TomlObject<?>> table,
		List<String> parents,
		StringBuilder builder,
		TomlParser parser
	) {
		StringBuilder parentKeyBuilder = new StringBuilder();
		for (String parent : parents)
		{
			parentKeyBuilder.append(parent);
			parentKeyBuilder.append('.');
		}

		for (String key : table.keySet())
		{
			String mapKey = key;
			if (quotedKeyPattern.matcher(key).find())
			{
				mapKey = "\"" + mapKey + "\"";
			}

			if (mapKey.contains("\\\\") || key.contains("\\\""));
			{
				mapKey = "'" + mapKey.replace("\\\\", "\\").replace("\\\"", "\"") + "'";
			}

			if (mapKey.length() == 0)
			{
				mapKey = "\"\"";
			}
			
			switch (table.get(key).getType())
			{
				case ARRAY -> builder.append(parentKeyBuilder + key + " = " + ((TomlArray) table.get(key)).serialize(parser));
				case BOOLEAN -> builder.append(parentKeyBuilder + key + " = " + table.get(key).serialize());
				case BYTE -> builder.append(parentKeyBuilder + key + (parser.getStoreInlineTypes() ? ": byte " : "") + " = " + ((TomlByte) table.get(key)).serialize(parser));
				case COMMENT -> builder.append(table.get(key).serialize());
				case DOUBLE -> builder.append(parentKeyBuilder + key + (parser.getStoreInlineTypes() ? ": double " : "") + " = " + table.get(key).serialize());
				case FLOAT -> builder.append(parentKeyBuilder + key + " = " + table.get(key).serialize());
				case INLINE_TABLE -> builder.append(parentKeyBuilder + key + " = " + table.get(key).serialize());
				case INTEGER -> builder.append(parentKeyBuilder + key + " = " + ((TomlInteger) table.get(key)).serialize(parser));
				case LOCAL_DATE -> builder.append(parentKeyBuilder + key + " = " + table.get(key).serialize());
				case LOCAL_DATE_TIME -> builder.append(parentKeyBuilder + key + " = " + table.get(key).serialize());
				case LOCAL_TIME -> builder.append(parentKeyBuilder + key + " = " + table.get(key).serialize());
				case LONG -> builder.append(parentKeyBuilder + key + (parser.getStoreInlineTypes() ? ": long " : "") + " = " + ((TomlLong) table.get(key)).serialize(parser));
				case OFFSET_DATE_TIME -> builder.append(parentKeyBuilder + key + " = " + table.get(key).serialize());
				case SHORT -> builder.append(parentKeyBuilder + key + (parser.getStoreInlineTypes() ? ": short " : "") + " = " + ((TomlShort) table.get(key)).serialize(parser));
				case STRING -> builder.append(parentKeyBuilder + key + " = " + table.get(key).serialize());
				case TABLE_ARRAY -> builder.append(table.get(key).serialize());
				case TABLE -> {
					TomlTable tableValue = (TomlTable) table.get(key);

					if (tableValue.isDiscrete())
					{
						builder.append(tableValue.serialize());
						for (int i = 0; i < tableValue.getNumberOfTrailingNewlines(); i++)
						{
							builder.append('\n');
						}

						if (tableValue.getComment() != null)
						{
							builder.append(" #" +tableValue.getComment());
						}

						//builder.append('\n');

						encodeTable(tableValue.get(), new ArrayList<>(), builder, parser);
						continue;
					}

					// Is a deque more correct here? Probably. TODO
					int index = parents.size();
					parents.add(key);
					encodeTable(tableValue.get(), parents, builder, parser);
					parents.remove(index);

					continue;
				}
			}

			if (table.get(key) instanceof Commentable c)
			{
				if (c.getComment() != null)
				{
					builder.append(" #" + c.getComment());
				}
			}

			if (table.get(key).getType() != TomlObjectType.TABLE_ARRAY)
			{
				for (int i = 0; i < table.get(key).getNumberOfTrailingNewlines(); i++)
				{
					builder.append('\n');
				}
			}

			//builder.append('\n');
		}
	}
}
