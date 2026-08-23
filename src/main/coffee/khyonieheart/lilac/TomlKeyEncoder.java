/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.Deque;
import java.util.Iterator;
import java.util.regex.Pattern;

final class TomlKeyEncoder
{
	private static final char KEY_SEPARATOR = '.';
	private static final Pattern BARE_KEY_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");

	private TomlKeyEncoder()
	{}

	static String tableHeader(
		Deque<String> keys
	) {
		return "[" + keysToString(keys) + "]";
	}

	static String arrayOfTablesHeader(
		Deque<String> keys
	) {
		return "[[" + keysToString(keys) + "]]";
	}

	static String keysToString(
		Deque<String> keys
	) {
		if (keys.isEmpty())
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();
		Iterator<String> keyIterator = keys.descendingIterator();
		while (keyIterator.hasNext())
		{
			builder.append(sanitizeKey(keyIterator.next()))
				.append(KEY_SEPARATOR);
		}

		return builder.substring(0, builder.length() - 1);
	}

	static String sanitizeKey(
		String key
	) {
		if (key.isEmpty())
		{
			return "\"\"";
		}

		if (BARE_KEY_PATTERN.matcher(key).matches() && !key.startsWith("-") && !Character.isDigit(key.charAt(0)))
		{
			return key;
		}
		
		return "\"" + escapeBasicString(key) + "\"";
	}

	private static String escapeBasicString(
		String value
	) {
		StringBuilder builder = new StringBuilder();
		for (char c : value.toCharArray())
		{
			appendEscapedCharacter(builder, c);
		}

		return builder.toString();
	}

	private static void appendEscapedCharacter(
		StringBuilder builder,
		char c
	) {
		switch (c)
		{
			case '\b' -> builder.append("\\b");
			case '\t' -> builder.append("\\t");
			case '\n' -> builder.append("\\n");
			case '\f' -> builder.append("\\f");
			case '\r' -> builder.append("\\r");
			case '"' -> builder.append("\\\"");
			case '\\' -> builder.append("\\\\");
			default -> {
				if (Character.getType(c) == Character.CONTROL)
				{
					appendUnicodeEscape(builder, c);
					return;
				}

				builder.append(c);
			}
		}
	}

	private static void appendUnicodeEscape(
		StringBuilder builder,
		char c
	) {
		String hex = Integer.toHexString(c).toUpperCase();
		builder.append("\\u");
		for (int i = hex.length(); i < 4; i++)
		{
			builder.append('0');
		}
		builder.append(hex);
	}
}
