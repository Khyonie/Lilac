package coffee.khyonieheart.lilac;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import coffee.khyonieheart.lilac.configuration.ArrayTypeContext;
import coffee.khyonieheart.lilac.configuration.TableTypeContext;
import coffee.khyonieheart.lilac.configuration.TomlLinkedHashMap;

public class LilacEncoder implements TomlEncoder
{
	private static final char TABLE_HEADER_START      = '[';
	private static final char TABLE_HEADER_END        = ']';
	private static final char KEY_SEPARATOR           = '.';
	private static final char INLINE_TABLE_START      = '{';
	private static final char INLINE_TABLE_END        = '}';
	private static final String ARRAY_OF_TABLES_START = "[[";
	private static final String ARRAY_OF_TABLES_END   = "]]";

	// Style settings
	private boolean alignEquals   = false; // Equal signs for a table will be aligned with extra whitespace
	private boolean breakArrays   = false; // Whether or not to break array values onto separate lines
	private boolean newlineTables = true; // Whether or not to put a newline before table headers

	@Override
	public String encode(Map<String, Object> data) 
	{
		StringBuilder builder = new StringBuilder();
		encode(data, data, new ArrayDeque<>(), builder);

		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	private void encode(
		Map<String, Object> rootData,
		Map<String, Object> data,
		Deque<String> keys,
		StringBuilder builder
	) {
		int maxKeyLength = Integer.MIN_VALUE;

		// If we want to align equals in columns, we need to find the longest key length;
		if (alignEquals)
		{
			for (String key : data.keySet())
			{
				if (key.length() > maxKeyLength)
				{
					maxKeyLength = key.length();
				}
			}
		}

		for (String key : data.keySet())
		{
			Object value = data.get(key);
			
			if (value instanceof Map)
			{
				if ((rootData instanceof TomlLinkedHashMap && ((TomlLinkedHashMap) rootData).getTableType((Map<String, Object>) value) != TableTypeContext.INLINE) || !(rootData instanceof TomlLinkedHashMap))
				{
					keys.push(key);
					
					if (newlineTables && !builder.isEmpty())
					{
						builder.append('\n');
					}

					builder.append(encodeTableHeader(keys))
						.append('\n');
					encode(rootData, (Map<String, Object>) value, keys, builder);

					keys.pop();
					continue;
				}
			}

			// Array of tables
			if (value instanceof List)
			{
				if (rootData instanceof TomlLinkedHashMap && ((TomlLinkedHashMap) rootData).getArrayType((List<Object>) value) == ArrayTypeContext.ARRAY_OF_TABLES)
				{
					keys.push(key);
					encodeArrayOfTables(rootData, (List<Map<String, Object>>) value, keys, builder);
					keys.pop();
					continue;
				}
			}

			builder.append(sanitizeKey(key));

			// Handle column alignment
			if (alignEquals)
			{
				builder.append(" ".repeat(maxKeyLength - key.length()));
			}

			builder.append(" = ")
				.append(encodeValue(rootData, value, 0))
				.append('\n');
		}
	}

	/** Branch method which takes any TOML-spec type and calls the appropriate encoder method. */
	@SuppressWarnings("unchecked")
	private String encodeValue(
		Map<String, Object> rootData,
		Object object,
		int tabDepth
	) {
		if (object instanceof Map)
		{
			return encodeTableInline(rootData, (Map<String, Object>) object, tabDepth + 1);
		}

		if (object instanceof List)
		{
			return encodeArray(rootData, (List<Object>) object, tabDepth + 1);
		}

		if (object instanceof String)
		{
			return encodeString((String) object);
		}

		if (object instanceof Float)
		{
			return encodeFloat((float) object);
		}

		if (object instanceof OffsetDateTime)
		{
			return encodeOffsetDateTime((OffsetDateTime) object);
		}

		if (object instanceof LocalDateTime)
		{
			return encodeDateTime((LocalDateTime) object);
		}

		if (object instanceof LocalDate)
		{
			return encodeDate((LocalDate) object);
		}

		if (object instanceof LocalTime)
		{
			return encodeTime((LocalTime) object);
		}

		return object.toString();
	}

	// --------------------------------------------------
	// Regular types
	// --------------------------------------------------
	
	/**
	 * Encodes a string.
	 */
	private String encodeString(
		String string
	) {
		if (string.contains("\n"))
		{
			if (containsControlCharacter(string))
			{
				return "\"\"\"" + escapeControlCharacters(string) + "\"\"\"";
			}

			return "'''" + string + "'''";
		}

		if (containsControlCharacter(string))
		{
			return "\"" + escapeControlCharacters(string) + "\"";
		}

		return "'" + string + "'";
	}

	private String encodeFloat(
		float f32
	) {
		if (f32 == Float.POSITIVE_INFINITY)
		{
			return "+inf";
		}

		if (f32 == Float.NEGATIVE_INFINITY)
		{
			return "-inf";
		}

		if (f32 != f32)
		{
			return "nan";
		}

		return Float.toString(f32);
	}

	/**
	 * Encodes a table header.
	 */
	private String encodeTableHeader(
		Deque<String> keys
	) {
		StringBuilder builder = new StringBuilder();
		builder.append(TABLE_HEADER_START)
			.append(keysToString(keys))
			.append(TABLE_HEADER_END);

		return builder.toString();
	}

	private String encodeOffsetDateTime(
		OffsetDateTime odt
	) {
		return encodeDateTime(odt.toLocalDateTime()) + odt.getOffset().toString();
	}

	private String encodeDateTime(
		LocalDateTime ldt
	) {
		return encodeDate(ldt.toLocalDate()) + "T" + encodeTime(ldt.toLocalTime());
	}

	private String encodeDate(
		LocalDate date
	) {
		String year = Integer.toString(date.getYear());
		while (year.length() < 4)
		{
			year = "0" + year;
		}

		String month = (date.getMonthValue() >= 10 ? "" : "0") + date.getMonthValue(); 
		String day = (date.getDayOfMonth() >= 10 ? "" : "0") + date.getDayOfMonth();

		return year + "-" + month + "-" + day;
	}

	private String encodeTime(
		LocalTime time
	) {
		String hours = (time.getHour() >= 10 ? "" : "0") + time.getHour();
		String minutes = (time.getMinute() >= 10 ? "" : "0") + time.getMinute();
		String seconds = (time.getSecond() >= 10 ? "" : "0") + time.getSecond();
		int nanos = time.getNano() / 1000000;

		return hours + ":" + minutes + ":" + seconds + (nanos == 0 ? "" : "." + nanos);
	}

	// --------------------------------------------------
	// Compound types
	// --------------------------------------------------

	/**
	 * Encodes an array of tables.
	 */
	private void encodeArrayOfTables(
		Map<String, Object> rootData,
		List<Map<String, Object>> data,
		Deque<String> keys,
		StringBuilder builder
	) {
		for (Map<String, Object> table : data)
		{
			if (!builder.isEmpty() && newlineTables)
			{
				builder.append('\n');
			}

			builder.append(ARRAY_OF_TABLES_START)
				.append(keysToString(keys))
				.append(ARRAY_OF_TABLES_END)
				.append('\n');

			encode(rootData, table, keys, builder);
		}
	}

	/** 
	 * Encodes an inline table.
	 */
	private String encodeTableInline(
		Map<String, Object> rootData,
		Map<String, Object> table,
		int tabDepth
	) {
		StringBuilder builder = new StringBuilder();
		builder.append(INLINE_TABLE_START);

		Iterator<Entry<String, Object>> entryIter = table.entrySet().iterator();
		while (entryIter.hasNext())
		{
			Entry<String, Object> entry = entryIter.next();
			String key = entry.getKey();
			Object value = entry.getValue();

			builder.append(sanitizeKey(key))
				.append(" = ")
				.append(encodeValue(rootData, value, tabDepth + 1));

			if (entryIter.hasNext())
			{
				builder.append(", ");
			}
		}

		builder.append(INLINE_TABLE_END);

		return builder.toString();
	}

	/**
	 * Encodes a regular array.
	 */
	private String encodeArray(
		Map<String, Object> rootData,
		List<Object> array,
		int tabDepth
	) {
		StringBuilder builder = new StringBuilder();
		builder.append(TABLE_HEADER_START);

		if (breakArrays && !array.isEmpty())
		{
			builder.append('\n');
			builder.append("\t".repeat(tabDepth));
		}

		Iterator<Object> iter = array.iterator();
		while (iter.hasNext())
		{
			Object object = iter.next();
			builder.append(encodeValue(rootData, object, tabDepth));

			if (iter.hasNext())
			{
				builder.append(", ");
				if (breakArrays)
				{
					builder.append('\n');
					builder.append("\t".repeat(tabDepth));
				}
			}
		}

		if (breakArrays && !array.isEmpty())
		{
			builder.append('\n');
			builder.append("\t".repeat(tabDepth - 1));
		}

		builder.append(TABLE_HEADER_END);

		return builder.toString();
	}

	//
	// Settings
	//
	
	public LilacEncoder setBreakArrays(
		boolean breakArrays
	) {
		this.breakArrays = breakArrays;
		return this;
	}

	public LilacEncoder setAlignValues(
		boolean alignValues
	) {
		this.alignEquals = alignValues;
		return this;
	}

	public LilacEncoder setAddNewlineBeforeTables(
		boolean addNewline
	) {
		this.newlineTables = addNewline;
		return this;
	}

	//
	// Utility
	//
	
	private static final Pattern BARE_KEY_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");

	private static String sanitizeKey(
		String key
	) {
		if (key.length() == 0)
		{
			return "\"\"";
		}

		if (!BARE_KEY_PATTERN.matcher(key).matches() || key.startsWith("-") || Character.isDigit(key.charAt(0)))
		{
			key = key.replace("\n", "\\n")
				.replace("\b", "\\b")
				.replace("\t", "\\t")
				.replace("\n", "\\n")
				.replace("\f", "\\f");

			if (key.contains("\""))
			{
				return "'" + key + "'";
			}
			
			return "\"" + key.replace("\n", "\\n") + "\"";
		}
		
		return key;
	}

	private static boolean containsControlCharacter(
		String string
	) {
		for (char c : string.toCharArray())
		{
			if (c == '\n')
			{
				continue;
			}

			if (Character.getType((int) c) == Character.CONTROL)
			{
				return true;
			}

			switch (c)
			{
				case '\'' -> { return true; }
				case '"' -> { return true; }
				default -> { continue; }
			}
		}

		return false;
	}

	private static String escapeControlCharacters(
		String string
	) {
		StringBuilder builder = new StringBuilder();

		boolean isEscaped = false;
		for (char c : string.toCharArray())
		{
			switch (c)
			{
				case '\b' -> builder.append("\\b"); // Backspace
				case '\t' -> builder.append("\\t"); // Tab
				case '\f' -> builder.append("\\f");
				case '\r' -> builder.append("\\r"); 
				case '\0' -> builder.append("\\u0000");
				case '\u007F' -> builder.append("\\u007F");
				case '\u001F' -> builder.append("\\u001F");
				case '"' -> {
					if (isEscaped)
					{
						builder.append('"');
						isEscaped = false;
						continue;
					}

					builder.append("\\\"");
				}
				case '\\' -> {
					if (isEscaped)
					{
						builder.append("\\\\");
						isEscaped = false;
						continue;
					}
					isEscaped = true;
					continue;
				}
				default -> builder.append(c);
			}

			isEscaped = false;
		}

		return builder.toString();
	}


	private static String keysToString(
		Deque<String> keys
	) {
		if (keys.isEmpty())
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (String key : keys.reversed())
		{
			builder.append(sanitizeKey(key));
			builder.append(KEY_SEPARATOR);
		}

		return builder.substring(0, builder.length() - 1);
	}
}
