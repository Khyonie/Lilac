/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

final class TomlStringEncoder
{
	private TomlStringEncoder()
	{}

	static String encode(
		String string
	) {
		if (string.contains("\n"))
		{
			if (requiresBasicString(string))
			{
				return "\"\"\"" + escapeBasicString(string, true) + "\"\"\"";
			}

			return "'''" + string + "'''";
		}

		if (requiresBasicString(string))
		{
			return "\"" + escapeBasicString(string, false) + "\"";
		}

		return "'" + string + "'";
	}

	private static boolean requiresBasicString(
		String string
	) {
		for (char c : string.toCharArray())
		{
			if (c == '\n')
			{
				continue;
			}

			if (Character.getType(c) == Character.CONTROL)
			{
				return true;
			}

			switch (c)
			{
				case '\'' -> { return true; }
				case '"' -> { return true; }
				default -> {}
			}
		}

		return false;
	}

	private static String escapeBasicString(
		String string,
		boolean multiline
	) {
		StringBuilder builder = new StringBuilder();
		for (char c : string.toCharArray())
		{
			appendEscapedCharacter(builder, c, multiline);
		}

		return builder.toString();
	}

	private static void appendEscapedCharacter(
		StringBuilder builder,
		char c,
		boolean multiline
	) {
		switch (c)
		{
			case '\b' -> builder.append("\\b");
			case '\t' -> builder.append("\\t");
			case '\n' -> builder.append(multiline ? "\n" : "\\n");
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
