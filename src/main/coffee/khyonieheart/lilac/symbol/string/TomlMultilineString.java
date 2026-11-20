/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol.string;

import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.TomlVersion;
import coffee.khyonieheart.lilac.decoder.LilacStrings;
import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolParsers;
import coffee.khyonieheart.lilac.symbol.SymbolType;
import coffee.khyonieheart.lilac.symbol.SymbolParser;

public class TomlMultilineString extends Symbol<String>
{
	private static final List<SymbolParser> NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.START_TABLE, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_INLINE_TABLE, SymbolParsers.END_ARRAY, SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY, SymbolParsers.END_OF_DOCUMENT);
	private static final List<SymbolParser> INLINE_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_INLINE_TABLE);
	private static final List<SymbolParser> ARRAY_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_ARRAY);
	private String value;

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		document.incrementPointer().incrementPointer().incrementPointer();

		StringBuilder builder = new StringBuilder();
		if (document.getCharAtPointer() == '\n')
		{
			document.incrementPointer();
		}

		boolean escaped = false;
		while (document.hasNext())
		{
			char current = document.getCharAndIncrement();

			if (current == '"' && !escaped)
			{
				int quotationMarks = 1;
				while (document.hasNext() && document.getCharAtPointer() == '"')
				{
					quotationMarks++;
					document.incrementPointer();

					if (quotationMarks > 5)
					{
						throw TomlSyntaxException.of("Too many quotation marks in multi-line TOML string", document);
					}
				}

				if (quotationMarks < 3)
				{
					builder.append("\"".repeat(quotationMarks));
					continue;
				}

				builder.append("\"".repeat(quotationMarks - 3));
				break;
			}

			if (escaped)
			{
				escaped = false;
				switch (current)
				{
					case '\n' -> skipThroughWhitespace(document, true);
					case '\r' -> skipThroughWhitespace(document, false);
					case '\t' -> skipThroughWhitespace(document, false);
					case ' ' -> skipThroughWhitespace(document, false);
					case 'e' -> {
						if (decoder.getVersion() == TomlVersion.V1_1_0)
						{
							builder.append('\u001B');
							continue;
						}

						throw TomlSyntaxException.of("ESC character literals '\\e' are not supported in TOML v1.0.0", document.hold());
					}
					case '"' -> builder.append(current);
					case 't' -> builder.append('\t');
					case 'n' -> builder.append('\n');
					case 'r' -> builder.append('\r');
					case 'f' -> builder.append('\f');
					case '\\' -> builder.append(current);
					case 'b' -> builder.append('\b');
					case 'U' -> { // UTF-24
						long codepoint = LilacStrings.readUTFCodepoint(document, 8);

						if (codepoint >= '\uD800' && codepoint <= '\uDFFF')
						{
							throw TomlSyntaxException.of("Unicode surrogates cannot be used in unicode characters", document.hold(5));
						}

						if (codepoint > (char) 0x0010FFFF)
						{
							throw TomlSyntaxException.of("Out-of-range unicode literal", document.hold(9));
						}

						current = document.getCharAtPointer();
						builder.append((char) codepoint);
						continue;
					}
					case 'u' -> { // UTF-16
						long codepoint = LilacStrings.readUTFCodepoint(document, 4);

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

						long codepoint = LilacStrings.readUTFCodepoint(document, 2);

						current = document.getCharAtPointer();
						builder.append((char) codepoint);
						continue;
					}
					default -> throw TomlSyntaxException.of("Unrecognized escape sequence \"\\" + current + "\"", document.hold());
				}
				continue;
			}

			if (current <= '\b' || (current > '\n' && current <= '\u001F') || current == '\u007F')
			{
				throw TomlSyntaxException.of("Control characters cannot be used in TOML strings (found \\u" + Integer.toHexString((int) current).toUpperCase() + ")", document);
			}

			if (current == '\\')
			{
				escaped = true;
				continue;
			}

			builder.append(current);
		}

		TomlMultilineString symbol = new TomlMultilineString();
		symbol.value = builder.toString();

		symbols.push(symbol);
		return symbol;
	}

	@Override
	public String getValue() 
	{
		return this.value;
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.VALUE;
	}

	@Override
	public List<SymbolParser> getNextSymbols(TomlDecoder decoder)
	{
		if (decoder.getContext() == null)
		{
			return NEXT_SYMBOLS;
		}

		return switch (decoder.getContext()) {
			case ARRAY -> ARRAY_NEXT_SYMBOLS;
			case INLINE_TABLE -> INLINE_NEXT_SYMBOLS;
			case ROOT -> NEXT_SYMBOLS;
		};
	}

	private static void skipThroughWhitespace(
		Document document,
		boolean startsOnNewline
	) {
		int pinDepth = document.getPinDepth();
		try {
			boolean foundNewline = startsOnNewline;
			document.pinPointer();
			while (document.hasNext())
			{
				char current = document.getCharAtPointer();

				if (current == '\n')
				{
					foundNewline = true;
					document.incrementPointer();
					continue;
				}

				if (current == '\r' || current == ' ' || current == '\t')
				{
					document.incrementPointer();
					continue;
				}

				if (!foundNewline)
				{
					document.rewindToPin();
					throw TomlSyntaxException.of("Line-ending backslash must be have a newline somewhere in the line", document);
				}

				document.removePin();
				return;
			}

			throw TomlSyntaxException.of("Unterminated multiline string", document);
		} finally {
			document.discardPinsAfter(pinDepth);
		}
	}
}
