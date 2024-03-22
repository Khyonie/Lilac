package coffee.khyonieheart.lilac.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import coffee.khyonieheart.lilac.TomlBuilder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.value.TomlArray;
import coffee.khyonieheart.lilac.value.TomlBoolean;
import coffee.khyonieheart.lilac.value.TomlByte;
import coffee.khyonieheart.lilac.value.TomlDouble;
import coffee.khyonieheart.lilac.value.TomlFloat;
import coffee.khyonieheart.lilac.value.TomlInlineTable;
import coffee.khyonieheart.lilac.value.TomlInteger;
import coffee.khyonieheart.lilac.value.TomlLong;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlObjectType;
import coffee.khyonieheart.lilac.value.TomlShort;
import coffee.khyonieheart.lilac.value.TomlString;
import coffee.khyonieheart.lilac.value.TomlTable;
import coffee.khyonieheart.lilac.value.formatting.TomlComment;

public class LilacTomlBuilder implements TomlBuilder
{
	private boolean preservesComments = false;
	private boolean uppercaseHex = false;
	private boolean storeJavaTypes = false;

	private Map<String, Pattern> compiledPatterns = new HashMap<>();
	private Pattern quotedKeyCharacters = Pattern.compile("[^A-Za-z0-9_-]");

	private Map<String, TomlObject<?>> commenttedTable = null;

	@Override
	public Map<String, TomlObject<?>> parseDocument(
		File file
	) 
		throws FileNotFoundException,
			TomlSyntaxException
	{
		Objects.requireNonNull(file);

		if (!file.exists())
		{
			throw new FileNotFoundException("No such file " + file.getAbsolutePath());
		}

		Map<String, TomlObject<?>> data = new LinkedHashMap<>();
		
		StringBuilder builder = new StringBuilder();
		try (Scanner scanner = new Scanner(file))
		{
			while (scanner.hasNextLine())
			{
				builder.append(scanner.nextLine());
				if (scanner.hasNext())
				{
					builder.append("\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return data;
		}

		String document = builder.toString();
		
		tomlDocument(document, new int[] { 0 }, data, new ArrayDeque<>());

		return data;
	}

	@Override
	public Map<String, TomlObject<?>> parseString(
		String string
	)
		throws TomlSyntaxException
	{
		Objects.requireNonNull(string);

		Map<String, TomlObject<?>> data = new LinkedHashMap<>();

		tomlDocument(string, new int[] { 0 }, data, new ArrayDeque<>());

		return data;
	}

	// Productions
	//-------------------------------------------------------------------------------- 

	/**
	 * TomlDocument:
	 * > {TomlConstruct}
	 * This production acts as the primary production goal.
	 */
	private void tomlDocument(
		String document, 
		int[] offset, 
		Map<String, TomlObject<?>> data,
		Deque<String> parents
	)
		throws TomlSyntaxException
	{
		consumeWhitespace(document, offset);
		while (tomlConstruct(document, offset, data, parents)) {
			if (!consumeWhitespace(document, offset))
			{
				break;
			}
			//System.out.println("Character at position " + offset[0] + ": \"" + document.charAt(offset[0]) + "\"");
		}

		if (offset[0] < (document.length() - 1))
		{
			throw new TomlSyntaxException("Failed to parse entire document, reached position " + offset[0] + " while the document length is " + document.length());
		}
	}

	/**
	 * TomlConstruct
	 * > FullLineComment 
	 * > DiscreteTable
	 * > KeyValuePair
	 */
	private boolean tomlConstruct(
		String document, 
		int[] offset, 
		Map<String, TomlObject<?>> data,
		Deque<String> parents
	) 
		throws TomlSyntaxException
	{
		//System.out.println("[ TomlConstruct ] Parsing new construct");

		Optional<TomlComment> comment = comment(document, offset);
		if (comment.isPresent())
		{
			consumeWhitespace(document, offset);
			int commentIndex = 0;
			String key = "TOML_FULL_LINE_COMMENT";

			Map<String, TomlObject<?>> target = (this.commenttedTable != null ? this.commenttedTable : data);
			while (target.containsKey(key + commentIndex))
			{
				commentIndex++;
			}

			if (this.preservesComments)
			{
				target.put(key + commentIndex, comment.get());
			}
			while (consumeNewLine(document, offset))
			{
				comment.get().incrementTrailingNewlines();
			}

			//System.out.println("[ TomlConstruct ] #################### Found a comment at position " + offset[0] + " ####################");
			return true;
		}

		//System.out.println("[ TomlConstruct ] Did not find a comment at position " + offset[0]);

		Optional<TomlTable> table = discreteTable(document, offset, data, parents);
		if (table.isPresent())
		{
			//System.out.println("[ TomlConstruct ] #################### Found a discrete table \"" + table.get().getKey() + "\" at position " + offset[0] + " ####################");
			consumeWhitespace(document, offset);

			TomlTable t = table.get();
			Map<String, TomlObject<?>> targetData = data;
			parents.clear();
			for (String k : t.getParents())
			{
				if (!targetData.containsKey(k))
				{
					targetData.put(k, new TomlTable(new ArrayList<>(parents)));
					//System.out.println("> [ TomlConstruct ] Inserted new subtable \"" + k + "\"");
				}

				parents.push(k);
				targetData = ((TomlTable) targetData.get(k).get()).get();
			}
			parents.push(t.getKey());

			if (targetData.containsKey(t.getKey()))
			{
				if (targetData.get(t.getKey()).getType() == TomlObjectType.TABLE)
				{
					throw new TomlSyntaxException("Duplicate table \"" + t.getKey() + "\"");
				}

				throw new TomlSyntaxException("Cannot redefine key \"" + t.getKey() + "\" as a table. Must provide a unique table name instead");
			}

			targetData.put(t.getKey(), t);
			return true;
		}

		//System.out.println("[ TomlConstruct ] Did not find a discrete table at position " + offset[0]);

		if (keyValuePair(document, offset, data, parents))
		{
			//System.out.println("→ [ TomlConstruct ] #################### Found a key/value pair at position " + offset[0] + " ####################");
			return true;
		}

		//System.out.println("← [ TomlConstruct ] Did not find a key/value pair at position " + offset[0]);

		return false;
	}

	/**
	 * DiscreteTable:
	 * > [ Key ] [# {Comment}]
	 */
	private Optional<TomlTable> discreteTable(
		String document, 
		int[] offset, 
		Map<String, TomlObject<?>> data,
		Deque<String> parents
	)
		throws TomlSyntaxException
	{
		if (!literal(document, "[", offset))
		{
			return Optional.empty();
		}

		Optional<List<String>> keys = key(document, offset, parents);
		if (keys.isEmpty())
		{
			throw new TomlSyntaxException("Expected a Key after table start at position " + offset[0]);
		}

		if (!literal(document, "]", offset))
		{
			throw new TomlSyntaxException("Expected a table end (\"]\") at position " + offset[0]);
		}

		String key = keys.get().get(keys.get().size() - 1);
		List<String> parentKeys = keys.get().subList(0, keys.get().size() - 1);
		TomlTable table = new TomlTable(key, parentKeys);

		this.commenttedTable = table.get();

		Optional<TomlComment> comment = comment(document, offset);
		if (comment.isPresent())
		{
			table.setComment(comment.get().get());
		}

		while (this.consumeNewLine(document, offset))
		{
			table.incrementTrailingNewlines();
		}

		consumeNewLine(document, offset);

		return Optional.of(table);
	}

	/**
	 * Key:
	 * > KeyIdentity {. KeyIdentity}
	 */
	private Optional<List<String>> key(
		String document, 
		int[] offset, 
		Deque<String> parents
	)
		throws TomlSyntaxException
	{
		Optional<String> identity = keyIdentity(document, offset);
		if (identity.isEmpty())
		{
			//System.out.println("[ Key ] No key identity found at position " + offset[0]);
			return Optional.empty();
		}

		List<String> subkeys = new ArrayList<>();
		subkeys.add(identity.get());

		consumeWhitespace(document, offset);
		while (literal(document, ".", offset))
		{
			consumeWhitespace(document, offset);
			identity = keyIdentity(document, offset);

			if (identity.isEmpty())
			{
				throw new TomlSyntaxException("Expected a KeyIdentity after dot at position " + offset[0]);
			}

			subkeys.add(identity.get());
		}

		return Optional.of(subkeys);
	}

	/**
	 * KeyIdentity:
	 * > NormalKey
	 * > QuotedKey
	 */
	private Optional<String> keyIdentity(
		String document, 
		int[] offset
	) {
		//System.out.println("[ KeyIdentity ] Searching for a normal key at position " + offset[0]);
		Optional<String> key = normalkey(document, offset);
		if (key.isPresent())
		{
			//System.out.println("[ KeyIdentity ] Found a normal key \"" + key.get() + "\"");
			return key;
		}

		//System.out.println("[ KeyIdentity ] Searching for a quoted key at position " + offset[0]);
		return quotedKey(document, offset);
	}

	/**
	 * NormalKey:
	 * Regex[ (A-Za-z0-9_-) ]
	 */
	private Optional<String> normalkey(
		String document, 
		int[] offset
	) {
		return regex(document, "([A-Za-z0-9_-]+)", offset);
	}

	/**
	 * QuotedKey:
	 * Regex[ ([\"'])((?:\\\1|.)*?)(\1) ]
	 */
	private Optional<String> quotedKey(
		String document, 
		int[] offset
	) {
		return regex(document, "([\"'])((?:\\\\1|.)*?)(\\1)", offset, 2);
	}

	/**
	 * KeyValuePair:
	 * > Key [: JavaType] = Value [# {Comment}]
	 */
	private boolean keyValuePair(
		String document, 
		int[] offset, 
		Map<String, TomlObject<?>> data,
		Deque<String> parents
	) 
		throws TomlSyntaxException
	{
		consumeWhitespace(document, offset);
		Optional<List<String>> key = key(document, offset, parents);

		if (key.isEmpty())
		{
			return false;
		}

		String parent;
		Deque<String> parentsCopy = new ArrayDeque<>(parents);
		Map<String, TomlObject<?>> targetTable = data;
		List<String> subparents = new ArrayList<>();

		while (!parentsCopy.isEmpty())
		{
			parent = parentsCopy.removeLast();
			//System.out.println(">>> Adding parent key copy " + parent);
			
			if (!targetTable.containsKey(parent))
			{
				targetTable.put(parent, new TomlTable(new ArrayList<>(subparents)));
			}

			subparents.add(parent);

			targetTable = ((TomlTable) targetTable.get(parent)).get();
		}

		for (String keyParent : key.get().subList(0, key.get().size() - 1))
		{
			//System.out.println(">>> Adding parent key copy " + keyParent);
			if (!targetTable.containsKey(keyParent))
			{
				targetTable.put(keyParent, new TomlTable(new ArrayList<>(subparents)));
			}

			subparents.add(keyParent);
			targetTable = ((TomlTable) targetTable.get(keyParent)).get();
		}

		this.commenttedTable = targetTable;

		String tableKey = key.get().get(key.get().size() - 1);

		consumeWhitespace(document, offset);

		String type = null;
		if (literal(document, ":", offset))
		{
			consumeWhitespace(document, offset);
			Optional<String> typeOption = javaType(document, offset);

			if (typeOption.isEmpty())
			{
				throw new TomlSyntaxException("Expected a Java type at position " + offset[0]);
			}

			type = typeOption.get();
		}

		consumeWhitespace(document, offset);
		if (!literal(document, "=", offset))
		{
			throw new TomlSyntaxException("Expected an \"=\" to pair the key \"" + key.get() + "\" and the following value together at position" + offset[0]);
		}

		consumeWhitespace(document, offset);
		Optional<TomlObject<?>> valueOption = value(document, offset, type);
		if (valueOption.isEmpty())
		{
			throw new TomlSyntaxException("Expected a value after \"=\" in key \"" + key.get() + "\" at position " + offset[0]);
		}

		TomlObject<?> value = valueOption.get();
		//System.out.println("[ KeyValuePair ] Key: " + tableKey + ", value is of type " + value.getType().name());

		if (!consumeWhitespace(document, offset))
		{
			targetTable.put(tableKey, value);
			while (this.consumeNewLine(document, offset))
			{
				value.incrementTrailingNewlines();
			}
			return true;
		}

		if (literal(document, "\n", offset))
		{
			targetTable.put(tableKey, value);
			while (this.consumeNewLine(document, offset))
			{
				value.incrementTrailingNewlines();
			}
			return true;
		}

		Optional<TomlComment> comment = comment(document, offset);
		if (comment.isPresent() && this.preservesComments && value instanceof Commentable c)
		{
			c.setComment(comment.get().get());
		}
		consumeWhitespace(document, offset);

		while (this.consumeNewLine(document, offset))
		{
			value.incrementTrailingNewlines();
		}

		targetTable.put(tableKey, value);

		consumeWhitespace(document, offset);
		return true;
	}

	/**
	 * Value:
	 * > String
	 * > Integer
	 * > Float
	 * > Boolean
	 * > OffsetDateTime
	 * > LocalDateTime
	 * > LocalDate
	 * > LocalTime
	 * > Array
	 * > InlineTable
	 */
	private Optional<TomlObject<?>> value(
		String document, 
		int[] offset,
		String type
	) 
		throws TomlSyntaxException
	{
		if (type != null)
		{
			Optional<? extends TomlObject<?>> obj = switch (type) {
				case "byte" -> integer(document, offset, TomlObjectType.BYTE);
				case "short" -> integer(document, offset, TomlObjectType.SHORT);
				case "integer" -> integer(document, offset, TomlObjectType.INTEGER);
				case "long" -> integer(document, offset, TomlObjectType.LONG);
				case "float" -> floatValue(document, offset);
				case "double" -> doubleValue(document, offset);
				default -> Optional.empty();
			};

			if (obj.isEmpty())
			{
				throw new TomlSyntaxException("Could not parse value with inline type \"" + type + "\" at position " + offset[0]);
			}

			return Optional.of(obj.get());
		}

		Optional<TomlString> string = string(document, offset);
		if (string.isPresent())
		{
			return Optional.of(string.get());
		}

		// We can't distinctively tell the difference between a float and a double in this context, so we default to float
		
		Optional<TomlFloat> floatValue = floatValue(document, offset);
		if (floatValue.isPresent())
		{
			return Optional.of(floatValue.get());
		}

		Optional<TomlObject<?>> integer = integer(document, offset, TomlObjectType.INTEGER);
		if (integer.isPresent())
		{
			return integer;
		}

		Optional<TomlBoolean> booleanValue = booleanValue(document, offset);
		if (booleanValue.isPresent())
		{
			return Optional.of(booleanValue.get());
		}

		// TODO Implement temporal types
		
		System.out.println("Checking for table");
		Optional<TomlInlineTable> table = inlineTable(document, offset);
		if (table.isPresent())
		{
			return Optional.of(table.get());
		}
		System.out.println("Table not found");

		Optional<TomlArray> array = array(document, offset);
		if (array.isPresent())
		{
			return Optional.of(array.get());
		}

		return Optional.empty();
	}

	/**
	 * String:
	 * > BasicString
	 * > MultiLineBasicString
	 * > LiteralString
	 * > MultiLineLiteralString
	 */
	private Optional<TomlString> string(
		String document, 
		int[] offset
	) {
		Optional<String> string = multiLineBasicString(document, offset);
		if (string.isPresent())
		{
			return Optional.of(new TomlString(string.get(), TomlStringType.MULTILINE_BASIC));
		}

		string = basicString(document, offset);
		if (string.isPresent())
		{
			return Optional.of(new TomlString(string.get()));
		}

		string = multiLineLiteralString(document, offset);
		if (string.isPresent())
		{
			return Optional.of(new TomlString(string.get(), TomlStringType.MULTILINE_LITERAL));
		}

		string = literalString(document, offset);
		if (string.isPresent())
		{
			return Optional.of(new TomlString(string.get(), TomlStringType.LITERAL));
		}

		return Optional.empty();
	}

	/**
	 * BasicString:
	 * > Regex[ \"((?:\\\"|.)*?)\" ]
	 */
	private Optional<String> basicString(
		String document, 
		int[] offset
	) {
		return regex(document, "\"((?:\\\"|.)*?)\"", offset, 1);
	}

	/**
	 * MultiLineBasicString:
	 * > " Regex[ \"\"\"((?:.|\n)*?)\"\"\" ] "
	 */
	private Optional<String> multiLineBasicString(
		String document, 
		int[] offset
	) {
		return regex(document, "\"\"\"((?:.|\n)*?)\"\"\"", offset, 1);
	}

	/**
	 * LiteralString:
	 * > Regex[ '((?:\\'|.)*?)' ]
	 */
	private Optional<String> literalString(
		String document, 
		int[] offset
	) {
		return regex(document, "'((?:\\'|.)*?)'", offset, 1);
	}

	/**
	 * MultiLineLiteralString:
	 * > Regex[ '''\n*((?:.|\n)*?)''' ]
	 */
	private Optional<String> multiLineLiteralString(
		String document, 
		int[] offset
	) {
		return regex(document, "'''\n*((?:.|\n)*?)'''", offset, 1);
	}

	/**
	 * Integer:
	 * > DecimalInteger
	 * > HexadecimalInteger
	 * > OctalInteger
	 * > BinaryInteger
	 */
	private Optional<TomlObject<?>> integer(
		String document, 
		int[] offset,
		TomlObjectType type
	) {
		Optional<TomlObject<?>> integer = hexadecimalInteger(document, offset, type);
		if (integer.isPresent())
		{
			return integer;
		}

		integer = octalInteger(document, offset, type);
		if (integer.isPresent())
		{
			return integer;
		}

		integer = binaryInteger(document, offset, type);
		if (integer.isPresent())
		{
			return integer;
		}

		integer = decimalInteger(document, offset, type);
		if (integer.isPresent())
		{
			return integer;
		}

		return Optional.empty();
	}

	/**
	 * DecimalInteger:
	 * Regex[ ([+-]?(?:_|\d)+) ]
	 */
	private Optional<TomlObject<?>> decimalInteger(
		String document, 
		int[] offset,
		TomlObjectType type
	) {
		Optional<String> integer = regex(document, "([+-]?(?:_|\\d)+)", offset);

		if (integer.isEmpty())
		{
			return Optional.empty();
		}

		return switch (type)
		{
			case BYTE -> Optional.of(new TomlByte(Byte.parseByte(integer.get())));
			case SHORT -> Optional.of(new TomlShort(Short.parseShort(integer.get())));
			case INTEGER -> Optional.of(new TomlInteger(Integer.parseInt(integer.get())));
			case LONG -> Optional.of(new TomlLong(Long.parseLong(integer.get())));
			default -> Optional.empty();
		};
	}

	/**
	 * HexadecimalInteger:
	 * Regex[ 0[xX]((?:_|[0-9a-fA-F])+) ]
	 */
	private Optional<TomlObject<?>> hexadecimalInteger(
		String document, 
		int[] offset,
		TomlObjectType type
	) {
		Optional<String> integer = regex(document, "0[xX]((?:_|[0-9a-fA-F])+)", offset);

		if (integer.isEmpty())
		{
			return Optional.empty();
		}

		NumberBase base = NumberBase.HEXADECIMAL;

		return switch (type)
		{
			case BYTE -> Optional.of(new TomlByte((byte) Short.parseShort(integer.get().substring(2), base.getRadix()), base));
			case SHORT -> Optional.of(new TomlShort((short) Integer.parseInt(integer.get().substring(2), base.getRadix()), base));
			case INTEGER -> Optional.of(new TomlInteger(Integer.parseInt(integer.get().substring(2), base.getRadix()), base));
			case LONG -> Optional.of(new TomlLong(Long.parseLong(integer.get().substring(2), base.getRadix()), base));
			default -> Optional.empty();
		};
	}

	/**
	 * OctalInteger:
	 * Regex[ 0o((?:_|[0-7])+) ]
	 */
	private Optional<TomlObject<?>> octalInteger(
		String document, 
		int[] offset,
		TomlObjectType type
	) {
		Optional<String> integer = regex(document, "0o((?:_|[0-7])+)", offset);

		if (integer.isEmpty())
		{
			return Optional.empty();
		}

		NumberBase base = NumberBase.OCTAL;

		return switch (type)
		{
			case BYTE -> Optional.of(new TomlByte((byte) Short.parseShort(integer.get().substring(2), base.getRadix()), base));
			case SHORT -> Optional.of(new TomlShort((short) Integer.parseInt(integer.get().substring(2), base.getRadix()), base));
			case INTEGER -> Optional.of(new TomlInteger(Integer.parseInt(integer.get().substring(2), base.getRadix()), base));
			case LONG -> Optional.of(new TomlLong(Long.parseLong(integer.get().substring(2), base.getRadix()), base));
			default -> Optional.empty();
		};
	}

	/**
	 * BinaryInteger:
	 * Regex[ 0[bB]((?:_|[01])+) ]
	 */
	private Optional<TomlObject<?>> binaryInteger(
		String document, 
		int[] offset,
		TomlObjectType type
	) {
		Optional<String> integer = regex(document, "0[bB]((?:_|[01])+)", offset);

		if (integer.isEmpty())
		{
			return Optional.empty();
		}

		NumberBase base = NumberBase.BINARY;

		return switch (type)
		{
			case BYTE -> Optional.of(new TomlByte((byte) Short.parseShort(integer.get().substring(2).replace("_", ""), base.getRadix()), base));
			case SHORT -> Optional.of(new TomlShort((short) Integer.parseInt(integer.get().substring(2).replace("_", ""), base.getRadix()), base));
			case INTEGER -> Optional.of(new TomlInteger(Integer.parseInt(integer.get().substring(2).replace("_", ""), base.getRadix()), base));
			case LONG -> Optional.of(new TomlLong(Long.parseLong(integer.get().substring(2).replace("_", ""), base.getRadix()), base));
			default -> Optional.empty();
		};
	}

	/**
	 * Float:
	 * > FractionalFloat
	 * > ExponentialFloat
	 */
	private Optional<TomlFloat> floatValue(
		String document, 
		int[] offset
	) {
		Optional<TomlFloat> floatOption = fractionalFloat(document, offset);
		if (floatOption.isPresent())
		{
			return floatOption;
		}

		floatOption = exponentialFloat(document, offset);
		if (floatOption.isPresent())
		{
			return floatOption;
		}

		return Optional.empty();
	}

	/**
	 * FractionalFloat:
	 * > Regex[ ([+-]?\d+\.\d+)([eE][+-]\d+)? ]
	 */
	private Optional<TomlFloat> fractionalFloat(
		String document, 
		int[] offset
	) {
		Optional<String> floatString = regex(document, "([+-]?\\d+\\.\\d+)([eE][+-]\\d+)?", offset);

		if (floatString.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(new TomlFloat(Float.parseFloat(floatString.get())));
	}

	/**
	 * ExponentialFloat:
	 * > Regex[ ([+-]?\d+)([eE][+-]\d+) ]
	 */
	private Optional<TomlFloat> exponentialFloat(
		String document,
		int[] offset
	) {
		Optional<String> floatString = regex(document, "([+-]?\\d+[eE][+-]\\d+)", offset);

		if (floatString.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(new TomlFloat(Float.parseFloat(floatString.get())));
	}

	/**
	 * Double:
	 * > FractionalDouble
	 * > ExponentialDouble
	 */
	private Optional<TomlDouble> doubleValue(
		String document, 
		int[] offset
	) {
		Optional<TomlDouble> doubleOption = fractionalDouble(document, offset);
		if (doubleOption.isPresent())
		{
			return doubleOption;
		}

		doubleOption = exponentialDouble(document, offset);
		if (doubleOption.isPresent())
		{
			return doubleOption;
		}

		return Optional.empty();
	}

	/**
	 * FractionalDouble:
	 * > Regex[ ([+-]?\d+\.\d+)([eE][+-]\d+)? ]
	 */
	private Optional<TomlDouble> fractionalDouble(
		String document, 
		int[] offset
	) {
		Optional<String> doubleString = regex(document, "([+-]?\\d+\\.\\d+)([eE][+-]\\d+)?", offset);

		if (doubleString.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(new TomlDouble(Double.parseDouble(doubleString.get())));
	}

	/**
	 * ExponentialDouble:
	 * > Regex[ ([+-]?\d+)([eE][+-]\d+) ]
	 */
	private Optional<TomlDouble> exponentialDouble(
		String document,
		int[] offset
	) {
		Optional<String> doubleString = regex(document, "([+-]?\\d+)([eE][+-]\\d+)", offset);

		if (doubleString.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(new TomlDouble(Double.parseDouble(doubleString.get())));
	}

	/**
	 * Boolean:
	 * > true 
	 * > false
	 */
	private Optional<TomlBoolean> booleanValue(
		String document,
		int[] offset
	) {
		if (literal(document, "true", offset))
		{
			return Optional.of(new TomlBoolean(true));
		}

		if (literal(document, "false", offset))
		{
			return Optional.of(new TomlBoolean(false));
		}

		return Optional.empty();
	}

	/**
	 * Array:
	 * > [ [Value {, Value}] ]
	 */
	private Optional<TomlArray> array(
		String document,
		int[] offset
	)
		throws TomlSyntaxException
	{
		if (!literal(document, "[", offset))
		{
			return Optional.empty();
		}

		consumeWhitespace(document, offset);

		List<TomlObject<?>> array = new ArrayList<>();
		Optional<TomlObject<?>> obj = value(document, offset, null);
		if (obj.isPresent())
		{
			array.add(obj.get());

			consumeWhitespace(document, offset);
			while (literal(document, ",", offset))
			{
				consumeWhitespace(document, offset);

				obj = value(document, offset, null);
				if (obj.isEmpty())
				{
					throw new TomlSyntaxException("Expected a value at position " + offset[0]);
				}

				array.add(obj.get());
				consumeWhitespace(document, offset);
			}
		}

		return Optional.empty();
	}

	/**
	 * InlineTable:
	 * > { [Key[: Javatype] = Value {, Key[: JavaType] = Value}] }
	 */
	private Optional<TomlInlineTable> inlineTable(
		String document,
		int[] offset
	)
		throws TomlSyntaxException
	{
		if (!literal(document, "{", offset))
		{
			return Optional.empty();
		}

		consumeWhitespace(document, offset);

		Optional<List<String>> keys = key(document, offset, new ArrayDeque<>());
		if (keys.isEmpty())
		{
			if (!literal(document, "}", offset))
			{
				throw new TomlSyntaxException("Expected a \"}\" to end inline table at position " + offset[0]);
			}

			return Optional.of(new TomlInlineTable(new LinkedHashMap<>()));
		}

		consumeWhitespace(document, offset);

		String type = null;
		if (literal(document, ":", offset))
		{
			consumeWhitespace(document, offset);
			
			Optional<String> typeOption = javaType(document, offset);
			if (typeOption.isEmpty())
			{
				throw new TomlSyntaxException("Expected JavaType at position " + offset[0]);
			}

			consumeWhitespace(document, offset);
			type = typeOption.get();
		}

		if (!literal(document, "=", offset))
		{
			throw new TomlSyntaxException("Expected a \"=\" to pair inline table KeyValuePair together at position " + offset[0]);
		}

		Optional<TomlObject<?>> valueOption = value(document, offset, type);

		if (valueOption.isEmpty())
		{
			throw new TomlSyntaxException("Expected a Value at position " + offset[0]);
		}

		consumeWhitespace(document, offset);

		Map<String, TomlObject<?>> rootData = new LinkedHashMap<>();
		Map<String, TomlObject<?>> targetData = rootData;
		String targetKey = keys.get().get(keys.get().size() - 1);
		for (String s : keys.get().subList(0, keys.get().size() - 1))
		{
			if (!targetData.containsKey(s))
			{
				targetData.put(s, new TomlInlineTable(new LinkedHashMap<>()));
			}

			targetData = ((TomlInlineTable) targetData.get(s)).get();
		}

		targetData.put(targetKey, valueOption.get());

		// Other key/value pairs
		while (literal(document, ",", offset))
		{
			targetData = rootData;
			consumeWhitespace(document, offset);

			keys = key(document, offset, new ArrayDeque<>());
			if (keys.isEmpty())
			{
				if (!literal(document, "}", offset))
				{
					throw new TomlSyntaxException("Expected a \"}\" to end inline table at position " + offset[0]);
				}

				targetData.put(targetKey, new TomlInlineTable(new LinkedHashMap<>()));
				continue;
			}

			consumeWhitespace(document, offset);

			type = null;
			if (literal(document, ":", offset))
			{
				consumeWhitespace(document, offset);
				
				Optional<String> typeOption = javaType(document, offset);
				if (typeOption.isEmpty())
				{
					throw new TomlSyntaxException("Expected JavaType at position " + offset[0]);
				}

				consumeWhitespace(document, offset);
				type = typeOption.get();
			}

			if (!literal(document, "=", offset))
			{
				throw new TomlSyntaxException("Expected a \"=\" to pair inline table KeyValuePair together at position " + offset[0]);
			}

			consumeWhitespace(document, offset);
			valueOption = value(document, offset, type);

			if (valueOption.isEmpty())
			{
				throw new TomlSyntaxException("Expected a Value at position " + offset[0]);
			}

			targetData = new LinkedHashMap<>();
			keys.get().get(keys.get().size() - 1);
			for (String s : keys.get().subList(0, keys.get().size() - 1))
			{
				if (!targetData.containsKey(s))
				{
					targetData.put(s, new TomlInlineTable(new LinkedHashMap<>()));
				}

				targetData = ((TomlInlineTable) targetData.get(s)).get();
			}

			targetData.put(targetKey, valueOption.get());
			consumeWhitespace(document, offset);
		}

		consumeWhitespace(document, offset);

		return Optional.of(new TomlInlineTable(targetData));
	}

	/**
	 * Comment:
	 * > # Regex[ (.*) ]
	 */
	private Optional<TomlComment> comment(
		String document,
		int[] offset
	) {
		if (!literal(document, "#", offset))
		{
			return Optional.empty();
		}

		Optional<String> comment = regex(document, "(.*)", offset);

		consumeNewLine(document, offset);
		if (comment.isEmpty())
		{
			return Optional.of(new TomlComment(""));
		}

		return Optional.of(new TomlComment(comment.get()));
	}

	// Literal constructs
	//-------------------------------------------------------------------------------- 

	/**
	 * JavaType:
	 * > byte
	 * > short
	 * > integer
	 * > long
	 * > float
	 * > double
	 */
	private Optional<String> javaType(
		String document, 
		int[] offset
	) {
		String[] possibleValues = new String[] { "byte", "short", "integer", "long", "float", "double" };
		for (String v : possibleValues)
		{
			if (document.substring(offset[0], offset[0] + v.length()).equals(v))
			{
				offset[0] += v.length();
				return Optional.of(v);
			}
		}

		return Optional.empty();
	}

	/**
	 * Implements any literal string/character
	 */
	private boolean literal(
		String document, 
		String literal,
		int[] offset
	) {
		//System.out.println("[ Literal ] Searching for literal \"" + literal + "\" (Literal at position: \"" + document.substring(offset[0], offset[0] + literal.length()) + "\")");
		if (document.substring(offset[0], offset[0] + literal.length()).equals(literal))
		{
			offset[0] += literal.length();
			//System.out.println("→ [ Literal ] Found literal, advancing offset to " + offset[0]);
			return true;
		}

		return false;
	}

	private Optional<String> regex(
		String document,
		String regex,
		int[] offset,
		int group
	) {
		//System.out.println("[ Regex ] Matching regex \"" + regex + "\"");
		if (!this.compiledPatterns.containsKey(regex))
		{
			this.compiledPatterns.put(regex, Pattern.compile(regex));
		}

		Matcher matcher = this.compiledPatterns.get(regex).matcher(document);
		if (!matcher.find(offset[0]))
		{
			//System.out.println("← [ Regex ] No match found");
			return Optional.empty();
		}

		for (int i = 0; i <= matcher.groupCount(); i++)
		{
			if (matcher.group(i) == null)
			{
				//System.out.println("[ Regex ] Skipping failure to match @ start " + matcher.start(i) + " → end " + matcher.end(i));
				continue;
			}
			//System.out.println("[ Regex ] Match " + i + ": \"" + matcher.group(i) + "\" @ start " + matcher.start(i) + " → end " + matcher.end(i) + " (capture length " + matcher.group(i).length() + ")");
		}

		if (matcher.start() != offset[0])
		{
			//System.out.println("← [ Regex ] Capture does not start at offset " + offset[0] + ", instead it started at " + matcher.start(group));
			return Optional.empty();
		}

		//System.out.println("[ Regex ] Found " + matcher.groupCount() + " matches");
		//System.out.println(" [ Regex ] Returning [ " + matcher.group(group) + " ]");
		offset[0] += matcher.group().length();
		//System.out.println("→ [ Regex ] Advanced offset to " + offset[0]);
		return Optional.of(matcher.group(group));
	}

	private Optional<String> regex(
		String document, 
		String regex,
		int[] offset
	) {
		return regex(document, regex, offset, 0);
	}

	private boolean consumeWhitespace(
		String document,
		int[] offset
	) {
		if (offset[0] >= document.length())
		{
			return false;
		}

		while (document.charAt(offset[0]) == ' ' || document.charAt(offset[0]) == '\t')
		{
			if (offset[0] == document.length() - 1)
			{
				//System.out.println("END OF DOCUMENT");
				return false;
			}

			offset[0]++;
			//System.out.println("→ [ Whitespace ] Consuming whitespace, new position is " + offset[0] + ", character is \"" + document.charAt(offset[0]) + "\"");
		}

		return true;
	}

	private boolean consumeNewLine(
		String document,
		int[] offset
	) {
		if (offset[0] >= document.length())
		{
			return false;
		}

		if (document.charAt(offset[0]) == '\n')
		{
			offset[0]++;
			return true;
		}

		return false;
	}

	// Boilerplate
	//-------------------------------------------------------------------------------- 

	@Override
	public TomlBuilder setPreserveComments(
		boolean setting
	) {
		this.preservesComments = setting;
		return this;
	}

	@Override
	public boolean getPreservesComments() 
	{
		return this.preservesComments;
	}

	@Override
	public TomlBuilder setUppercaseHexadecimal(
		boolean setting
	) {
		this.uppercaseHex = setting;
		return this;
	}

	@Override
	public boolean getUppercaseHexadecimal() 
	{
		return this.uppercaseHex;
	}

	@Override
	public TomlBuilder setStoreJavaTypes(
		boolean setting
	) {
		this.storeJavaTypes = setting;

		return this;
	}

	@Override
	public boolean getStoreJavaTypes()
	{
		return this.storeJavaTypes;
	}

	@Override
	public String toTomlFromTable(
		Map<String, TomlObject<?>> data
	) {
		StringBuilder builder = new StringBuilder();
		mapTableToString(data, builder, new ArrayList<>());

		return builder.toString();
	}

	private void mapTableToString(
		Map<String, TomlObject<?>> data,
		StringBuilder builder,
		List<String> parents
	) {
		StringBuilder parentKeyBuilder = new StringBuilder();
		for (String parent : parents)
		{
			parentKeyBuilder.append(parent);
			parentKeyBuilder.append('.');
		}

		String key;
		TomlObject<?> value;
		for (Entry<String, TomlObject<?>> entry : data.entrySet())
		{
			key = entry.getKey();
			value = entry.getValue();

			if (this.quotedKeyCharacters.matcher(key).find())
			{
				key = "\"" + key + "\"";
			}

			if (key.length() == 0)
			{
				key = "\"\"";
			}

			//System.out.println("Serializing key " + key);
			
			switch (value.getType())
			{
				case ARRAY -> builder.append(parentKeyBuilder + key + " = " + value.serialize());
				case BOOLEAN -> builder.append(parentKeyBuilder + key + " = " + value.serialize());
				case BYTE -> builder.append(parentKeyBuilder + key + (this.storeJavaTypes ? ": byte" : "") + " = " + value.serialize());
				case COMMENT -> builder.append(value.serialize());
				case DOUBLE -> builder.append(parentKeyBuilder + key + (this.storeJavaTypes ? ": double" : "") + " = " + value.serialize());
				case FLOAT -> builder.append(parentKeyBuilder + key + " = " + value.serialize());
				case INTEGER -> builder.append(parentKeyBuilder + key + " = " + value.serialize());
				case LOCAL_DATE -> throw new UnsupportedOperationException("java.lang.temporal types are not supported yet");
				case LOCAL_DATE_TIME -> throw new UnsupportedOperationException("java.lang.temporal types are not supported yet");
				case LOCAL_TIME -> throw new UnsupportedOperationException("java.lang.temporal types are not supported yet");
				case OFFSET_DATE_TIME -> throw new UnsupportedOperationException("java.lang.temporal types are not supported yet");
				case LONG -> builder.append(parentKeyBuilder + key + (this.storeJavaTypes ? ": long" : "") + " = " + value.serialize());
				case SHORT -> builder.append(parentKeyBuilder + key + (this.storeJavaTypes ? ": short" : "") + " = " + value.serialize());
				case STRING -> builder.append(parentKeyBuilder + key + " = " + value.serialize());
				case INLINE_TABLE -> builder.append(parentKeyBuilder + key + " = " + value.serialize());
				case TABLE -> {
					if (((TomlTable) value).isDiscrete())
					{
						builder.append(value.serialize());
						for (int i = 0; i < value.getNumberOfTrailingNewlines(); i++)
						{
							builder.append("\n");
						}

						mapTableToString(((TomlTable) value).get(), builder, new ArrayList<>());
						continue;
					}

					int index = parents.size();
					parents.add(key);
					mapTableToString(((TomlTable) value).get(), builder, parents);
					parents.remove(index);
					continue;
				}
			}

			if (value instanceof Commentable commentValue)
			{
				if (commentValue.getComment() != null && this.preservesComments)
				{
					builder.append(" #" + commentValue.getComment());
				}
			}

			for (int i = 0; i < value.getNumberOfTrailingNewlines(); i++)
			{
				builder.append("\n");
			}

			builder.append('\n');
		}
	}

	@Override
	public String toToml(
		Map<String, Object> data
	) {
		Objects.requireNonNull(data);

		// Convert all values into TomlObject<?>s
		Map<String, TomlObject<?>> tomlData = new LinkedHashMap<>();
		mapToToml(data, tomlData, new ArrayDeque<>());

		return toTomlFromTable(tomlData);
	}

	@SuppressWarnings("unchecked")
	private void mapToToml(
		Map<String, Object> data,
		Map<String, TomlObject<?>> tomlData,
		Deque<String> parents
	) {
		for (String key : data.keySet())
		{
			if (data.get(key) instanceof Map)
			{
				parents.push(key);
				Map<String, TomlObject<?>> subtable = new LinkedHashMap<>();
				mapToToml((Map<String, Object>) data.get(key), subtable, parents);

				TomlTable table = new TomlTable(new ArrayList<>(parents));
				tomlData.put(key, table);
				parents.pop();
				continue;
			}

			Object value = data.get(key);
			if (value == null)
			{
				continue;
			}

			TomlObject<?> tomlObject = mapToTomlObject(value);

			if (tomlObject == null)
			{
				parents.push(key);
				TomlTable objectTable = new TomlTable(new ArrayList<>(parents));
				parents.pop();

				objectTable.get().putAll(serializeObject(value));
			}

			tomlData.put(key, tomlObject);
		}
	}

	private TomlObject<?> mapToTomlObject(
		Object value
	) {
		if (value.getClass().equals(Byte.class))
		{
			return new TomlByte((byte) value);
		}

		if (value.getClass().equals(Short.class))
		{
			return new TomlShort((short) value);
		}

		if (value.getClass().equals(Integer.class))
		{
			return new TomlInteger((int) value);
		}

		if (value.getClass().equals(Long.class))
		{
			return new TomlLong((long) value);
		}

		if (value.getClass().equals(String.class))
		{
			return new TomlString((String) value);
		}

		if (value.getClass().equals(Boolean.class))
		{
			return new TomlBoolean((boolean) value);
		}

		if (value.getClass().equals(Float.class))
		{
			return new TomlFloat((float) value);
		}

		if (value.getClass().equals(Double.class))
		{
			return new TomlDouble((double) value);
		}

		if (value instanceof List list)
		{
			List<TomlObject<?>> tomlList = new ArrayList<>();

			for (Object obj : list)
			{
				tomlList.add(mapToTomlObject(obj));
			}

			return new TomlArray(tomlList);
		}

		if (value instanceof Map<?, ?> map)
		{
			Map<String, Object> copyData = new HashMap<>();
			
			map.forEach((key, v) -> copyData.put(key.toString(), v));

			Map<String, TomlObject<?>> tomlData = new LinkedHashMap<>();
			mapToToml(copyData, tomlData, new ArrayDeque<>());

			return new TomlInlineTable(tomlData);
		}

		/**
		if (value.getClass().isArray())
		{
			List<TomlObject<?>> tomlList = new ArrayList<>();

			for (Object obj : (Object[]) value)
			{
				tomlList.add(mapToTomlObject(obj));
			}

			return new TomlArray(tomlList);
		}
		*/

		return null;
	}

	@Override
	public Map<String, TomlObject<?>> serializeObject(
		Object object
	) {
		Objects.requireNonNull(object);

		Map<String, TomlObject<?>> tomlData = new LinkedHashMap<>();
		
		// Public/inheritted fields
		for (Field f : object.getClass().getFields())
		{
			if (!f.isAnnotationPresent(LilacExpose.class))
			{
				continue;
			}

			if (!f.getAnnotation(LilacExpose.class).serialize())
			{
				continue;
			}

			try {
				f.setAccessible(true);
				if (f.get(object) == null)
				{
					continue;
				}

				tomlData.put(f.getName(), mapToTomlObject(f.get(object)));
				System.out.println("Serialized public/inheritted field " + f.getName() + " with type " + f.getType().getName() + " (Null? " + (tomlData.get(f.getName()) == null) + ")");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}

		// Protected/private/default fields
		for (Field f : object.getClass().getDeclaredFields())
		{
			if (!f.isAnnotationPresent(LilacExpose.class))
			{
				continue;
			}

			if (!f.getAnnotation(LilacExpose.class).serialize())
			{
				continue;
			}

			try {
				f.setAccessible(true);
				if (f.get(object) == null)
				{
					continue;
				}

				tomlData.put(f.getName(), mapToTomlObject(f.get(object)));
				System.out.println("Serialized invisible field " + f.getName() + " with type " + f.getType().getName() + " (Null? " + (tomlData.get(f.getName()) == null) + ")");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}

		return tomlData;
	}
}
