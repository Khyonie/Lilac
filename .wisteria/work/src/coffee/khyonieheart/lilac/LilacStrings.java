package coffee.khyonieheart.lilac;

import java.util.Objects;

public class LilacStrings
{
	private static int HEXADECIMAL_BASE = 16;
	private static char QUOTATION_MARK = '"';
	private static char APOSTROPHE = '\'';

	public static String tryParseQuotedString(
		Document document,
		TomlDecoder decoder
	) {
		Objects.requireNonNull(document);

		if (document.skipToNextImportant())
		{
			throw TomlSyntaxException.of("Unexpected end of document", document);
		}

		if (document.getCharAtPointer() != '"')
		{
			return null;
		}

		document.pinPointer();
		document.incrementPointer();

		boolean escaped = false;
		char current;
		StringBuilder builder = new StringBuilder();
		while (true) 
		{
			if (!document.hasNext())
			{
				document.rewindToPin();
				throw TomlSyntaxException.of("Unterminated string to end of document", document);
			}

			current = document.getCharAndIncrement();

			if (current == QUOTATION_MARK && !escaped)
			{
				break;
			}

			// Illegal characters
			if (current == '\n')
			{
				document.rewindToPin();
				throw TomlSyntaxException.of("Unterminated string", document);
			}

			if (current <= '\b' || (current > '\n' && current <= '\u001F') || current == '\u007F')
			{
				throw TomlSyntaxException.of("Control characters cannot be used in TOML strings (found \\u" + Integer.toHexString((int) current).toUpperCase() + ")", document);
			}

			if (escaped)
			{
				escaped = false;
				switch (current)
				{
					case '"' -> builder.append(current);
					case 'e' -> {
						if (decoder.getVersion() == TomlVersion.V1_1_0)
						{
							builder.append('\u001B');
							continue;
						}

						throw TomlSyntaxException.of("ESC character literals '\\e' are not supported in TOML v1.0.0", document.hold());
					}
					case 't' -> builder.append('\t');
					case 'n' -> builder.append('\n');
					case 'r' -> builder.append('\r');
					case 'f' -> builder.append('\f');
					case '\\' -> builder.append(current);
					case 'b' -> builder.append('\b');
					case 'U' -> { // UTF-24
						long codepoint = readUTFCodepoint(document, 8);

						if (codepoint >= '\uD800' && codepoint <= '\uDFFF')
						{
							throw TomlSyntaxException.of("Unicode surrogates cannot be used in unicode characters", document.hold(5));
						}

						if (codepoint > 0x0010FFFF)
						{
							throw TomlSyntaxException.of("Out-of-range unicode literal", document.hold(9));
						}

						current = document.getCharAtPointer();
						builder.append((char) codepoint);
						continue;
					}
					case 'u' -> { // UTF-16
						long codepoint = readUTFCodepoint(document, 4);

						if (codepoint >= '\uD800' && codepoint <= '\uDFFF')
						{
							throw TomlSyntaxException.of("Unicode surrogates cannot be used in unicode characters", document.hold(5));
						}

						current = document.getCharAtPointer();
						builder.append((char) codepoint);
						continue;
					}
					case 'x' -> { // UTF-8 hex
						if (decoder.getVersion() != TomlVersion.V1_1_0)
						{
							throw TomlSyntaxException.of("Hex escaped character literals '\\x' are not supported in TOML v1.0.0", document.hold());
						}

						long codepoint = readUTFCodepoint(document, 2);

						current = document.getCharAtPointer();
						builder.append((char) codepoint);
						continue;
					}
					default -> throw TomlSyntaxException.of("Unrecognized escape sequence \"\\" + current + "\"", document.hold().hold());
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

	/**
	 * Attempts to parse a literal string.
	 */
	public static String tryParseLiteralString(
		Document document
	) {
		Objects.requireNonNull(document);

		if (document.skipToNextImportant())
		{
			throw TomlSyntaxException.of("Unexpected end of document", document);
		}

		if (document.getCharAtPointer() != APOSTROPHE)
		{
			return null;
		}

		document.pinPointer();
		document.incrementPointer();

		char current;
		StringBuilder builder = new StringBuilder();
		while (true) // This scares me
		{
			if (!document.hasNext())
			{
				document.rewindToPin();
				throw TomlSyntaxException.of("Unterminated string", document);
			}

			current = document.getCharAndIncrement();

			if (current == APOSTROPHE)
			{
				break;
			}

			if (current <= '\b' || (current >= '\n' && current <= '\u001F') || current == '\u007F')
			{
				throw TomlSyntaxException.of("Control characters cannot be used in TOML strings", document);
			}

			builder.append(current);
		}

		return builder.toString();
	}

	public static long readUTFCodepoint(
		Document document,
		int length
	) {
		char current;
		String codepoint = new String();
		for (int i = 0; i < length; i++)
		{
			if (!document.hasNext())
			{
				throw TomlSyntaxException.of("Unexpected end of document", document);
			}

			current = document.getCharAndIncrement();
			try {
				Byte.parseByte("" + current, HEXADECIMAL_BASE);

				codepoint += current;
			} catch (NumberFormatException e) {
				throw TomlSyntaxException.of("Illegal character \"" + current + "\" in unicode sequence", document);
			}
		}

		return Long.parseLong(codepoint, HEXADECIMAL_BASE);
	}
}
