/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol.string;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.LilacStrings;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.TomlVersion;
import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolType;
import coffee.khyonieheart.lilac.symbol.TomlArraySeparator;
import coffee.khyonieheart.lilac.symbol.TomlComment;
import coffee.khyonieheart.lilac.symbol.TomlEndArray;
import coffee.khyonieheart.lilac.symbol.TomlEndInlineTable;
import coffee.khyonieheart.lilac.symbol.TomlEndOfDocument;
import coffee.khyonieheart.lilac.symbol.TomlStartTable;
import coffee.khyonieheart.lilac.symbol.key.TomlBareKey;
import coffee.khyonieheart.lilac.symbol.key.TomlLiteralKey;
import coffee.khyonieheart.lilac.symbol.key.TomlQuotedKey;

public class TomlMultilineString extends Symbol<String>
{
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlComment.class, TomlStartTable.class, TomlArraySeparator.class, TomlEndInlineTable.class, TomlEndArray.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class, TomlEndOfDocument.class);
	private static final Collection<Class<? extends Symbol<?>>> INLINE_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndInlineTable.class);
	private static final Collection<Class<? extends Symbol<?>>> ARRAY_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndArray.class);
	private String value;

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		document.pinPointer();
		for (int i = 0; i < 3; i++)
		{
			if (document.getCharAndIncrement() != '"')
			{
				document.rewindToPin();
				return false; 
			}
		}

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
				throw TomlSyntaxException.of("Control characters cannot be used in TOML strings", document);
			}

			if (current == '\\')
			{
				escaped = true;
				continue;
			}

			builder.append(current);
		}

		value = builder.toString();

		symbols.push(this);
		return true;
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
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
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

	private void skipThroughWhitespace(
		Document document,
		boolean startsOnNewline
	) {
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
	}
}
