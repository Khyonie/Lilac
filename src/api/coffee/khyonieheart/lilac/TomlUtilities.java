package coffee.khyonieheart.lilac;

import java.util.ArrayDeque;
import java.util.Deque;

class TomlUtilities
{
	/**
	 * Splits a TOML fully-qualified key into its parsed key segments.
	 *
	 * @param fullyQualifiedKey Fully-qualified TOML key.
	 *
	 * @return Parsed key segments in lookup order.
	 * @throws IllegalArgumentException Thrown if the key is not syntactically valid.
	 */
	public static String[] fullyQualifiedKeyToArray(
		String fullyQualifiedKey
	) {
		int[] pointer = new int[] { 0 };
		Deque<String> keys = new ArrayDeque<>();
		boolean sawKey = false;
		boolean expectKey = true;
		while (pointer[0] < fullyQualifiedKey.length())
		{
			skipThroughWhitespace(fullyQualifiedKey, pointer);
			if (pointer[0] >= fullyQualifiedKey.length())
			{
				break;
			}

			char current = fullyQualifiedKey.charAt(pointer[0]);
			if (current == '.')
			{
				if (expectKey || !sawKey)
				{
					throw new IllegalArgumentException("Invalid key \"" + fullyQualifiedKey + "\". A key separator must follow a key.");
				}

				pointer[0]++;
				expectKey = true;
				continue;
			}

			if (!expectKey)
			{
				throw new IllegalArgumentException("Invalid key \"" + fullyQualifiedKey + "\". Expected a key separator.");
			}

			switch (current)
			{
				case '\'' -> keys.push(parseLiteralKey(fullyQualifiedKey, pointer));
				case '"' -> keys.push(parseQuotedKey(fullyQualifiedKey, pointer));
				default -> {
					if ((current >= '0' && current <= '9') || (current >= 'A' && current <= 'Z') || (current >= 'a' && current <= 'z') || current == '-' || current == '_')
					{
						keys.push(parseBareKey(fullyQualifiedKey, pointer));
						break;
					}

					throw new IllegalArgumentException("Invalid key \"" + fullyQualifiedKey + "\", a key must start with an alphanumeric character, a dash, an underscore, a quotation mark, or an apostrophe (a-zA-Z0-9_-'\")");
				}
			}

			sawKey = true;
			expectKey = false;
		}

		if (!sawKey)
		{
			throw new IllegalArgumentException("Invalid key \"" + fullyQualifiedKey + "\". A key must not be empty.");
		}

		if (expectKey)
		{
			throw new IllegalArgumentException("Invalid key \"" + fullyQualifiedKey + "\". A key separator must be followed by a key.");
		}

		String[] keysArray = new String[keys.size()];
		for (int i = 0; i < keysArray.length; i++)
		{
			keysArray[i] = keys.removeLast();
		}

		return keysArray;
	}

	/**
	 * Attempts to parse a bare key.
	 */
	private static String parseBareKey(
		String fullyQualifiedKey,
		int[] pointer
	) {
		char currentChar;
		StringBuilder builder = new StringBuilder();

		// Parse key
		while (pointer[0] < fullyQualifiedKey.length())
		{
			currentChar = fullyQualifiedKey.charAt(pointer[0]);

			if (currentChar == '.')
			{
				break;
			}
			
			if ((currentChar >= 'a' && currentChar <= 'z') || (currentChar >= 'A' && currentChar <= 'Z') || (currentChar >= '0' && currentChar <= '9') || currentChar == '_' || currentChar == '-')
			{
				builder.append(currentChar);
				pointer[0]++;
				continue;
			}

			throw new IllegalArgumentException("Illegal character \"" + currentChar + "\" in bare key. Consider wrapping the key in quotation marks.");
		}

		// Blank bare keys are not allowed
		if (builder.length() == 0)
		{
			throw new IllegalArgumentException("Illegal key \"" + fullyQualifiedKey + "\", a bare key must not be empty. If it must be empty, then consider using an empty set of quotes \"\".");
		}

		return builder.toString();
	}

	/**
	 * Attempts to parse a quoted key.
	 */
	private static String parseQuotedKey(
		String fullyQualifiedKey,
		int[] pointer
	) {
		pointer[0]++;

		if (pointer[0] >= fullyQualifiedKey.length())
		{
			throw new IllegalArgumentException("Unterminated string in key");
		}

		boolean escaped = false;
		char current;
		StringBuilder builder = new StringBuilder();
		while (true) 
		{
			if (pointer[0] >= fullyQualifiedKey.length())
			{
				throw new IllegalArgumentException("Unterminated string to end of key");
			}

			current = fullyQualifiedKey.charAt(pointer[0]++);

			if (current == '"' && !escaped)
			{
				break;
			}

			// Illegal characters
			if (current <= '\b' || (current > '\n' && current <= '\u001F') || current == '\u007F')
			{
				throw new IllegalArgumentException("Control characters cannot be used in TOML strings (found \\u" + Integer.toHexString((int) current).toUpperCase() + ")");
			}

			if (escaped)
			{
				escaped = false;
				switch (current)
				{
					case '"' -> builder.append(current);
					case 't' -> builder.append('\t');
					case 'n' -> builder.append('\n');
					case 'r' -> builder.append('\r');
					case 'f' -> builder.append('\f');
					case '\\' -> builder.append(current);
					case 'b' -> builder.append('\b');
					case 'U' -> { // UTF-32
						long codepoint = readUTFCodepoint(fullyQualifiedKey, pointer, 8);

						if (codepoint >= '\uD800' && codepoint <= '\uDFFF')
						{
							throw new IllegalArgumentException("Unicode surrogates cannot be used as a codepoint");
						}

						if (codepoint > 0x0010FFFF)
						{
							throw new IllegalArgumentException("Out-of-range unicode literal");
						}

						builder.appendCodePoint((int) codepoint);
						continue;
					}
					case 'u' -> { // UTF-16
						long codepoint = readUTFCodepoint(fullyQualifiedKey, pointer, 4);

						if (codepoint >= '\uD800' && codepoint <= '\uDFFF')
						{
							throw new IllegalStateException("Unicode surrogates cannot be used as a codepoint");
						}

						builder.appendCodePoint((int) codepoint);
						continue;
					}
					default -> throw new IllegalArgumentException("Unrecognized escape sequence \"\\" + current + "\"");
				}

				continue;
			}

			if (current == '\\')
			{
				escaped = true;
				continue;
			}

			builder.append(current);
		}

		return builder.toString();
	}

	private static String parseLiteralKey(
		String fullyQualifiedKey,
		int[] pointer
	) {
		pointer[0]++;

		if (pointer[0] >= fullyQualifiedKey.length())
		{
			throw new IllegalArgumentException("Unterminated string in key");
		}
		
		char current;
		StringBuilder builder = new StringBuilder();
		while (true) // This scares me
		{
			if (pointer[0] >= fullyQualifiedKey.length())
			{
				throw new IllegalArgumentException("Unterminated literal string to end of key");
			}

			current = fullyQualifiedKey.charAt(pointer[0]++);

			if (current == '\'')
			{
				break;
			}

			if (current <= '\b' || (current > '\n' && current <= '\u001F') || current == '\u007F')
			{
				throw new IllegalArgumentException("Control characters cannot be used in TOML strings (found \\u" + Integer.toHexString((int) current).toUpperCase() + ")");
			}

			builder.append(current);
		}

		return builder.toString();
	}
	
	/**
	 * Skips through leading whitespace.
	 *
	 * @return Returns true if the end of the fully qualified key was reached, false otherwise.
	 */
	private static boolean skipThroughWhitespace(
		String fullyQualifiedKey,
		int[] pointer
	) {
		while (pointer[0] < fullyQualifiedKey.length())
		{
			if (!Character.isWhitespace(fullyQualifiedKey.charAt(pointer[0])))
			{
				return true;
			}
			pointer[0]++;
		}

		return false;
	}

	private static long readUTFCodepoint(
		String fullyQualifiedKey,
		int[] pointer,
		int length
	) {
		char current;
		String codepoint = new String();
		for (int i = 0; i < length; i++)
		{
			if (pointer[0] >= fullyQualifiedKey.length())
			{
				throw new IllegalArgumentException("Not enough characters present for UTF codepoint");
			}

			current = fullyQualifiedKey.charAt(pointer[0]++);
			try {
				Byte.parseByte("" + current, 16);

				codepoint += current;
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Illegal character \"" + current + "\" in unicode sequence");
			}
		}

		return Long.parseLong(codepoint, 16);

	}
}
