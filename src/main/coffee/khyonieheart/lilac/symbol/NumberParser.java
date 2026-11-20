/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */
package coffee.khyonieheart.lilac.symbol;

import java.util.Deque;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.TomlWarning;

/** Shared reader for floating-point and decimal integer literals. */
final class NumberParser
{
	private static boolean warned = false;

	private NumberParser() {}

	static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		Symbol<?> symbol = tryParse(document, decoder, symbols, true);
		// Keep the existing radix reader and fallback diagnostics.
		return symbol != null ? symbol : TomlDecimal.tryParse(document, decoder, symbols);
	}

	static Symbol<?> tryParseFloat(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		return tryParse(document, decoder, symbols, false);
	}

	private static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols, boolean allowInteger)
	{
		int pinDepth = document.getPinDepth();
		try {
			if ((document.getCharAtPointer() < '0' || document.getCharAtPointer() > '9') && document.getCharAtPointer() != '-' && document.getCharAtPointer() != '+' && document.getCharAtPointer() != 'n' && document.getCharAtPointer() != 'i')
			{
				return null;
			}

			StringBuilder builder = new StringBuilder();
			document.pinPointer();

			if (document.getCharAtPointer() == '+' || document.getCharAtPointer() == '-')
			{
				builder.append(document.getCharAtPointer());
				document.incrementPointer();
			}

			// Infinity literal
			if (document.getCharAtPointer() == 'i')
			{
				document.incrementPointer();
				if (document.getCharAndIncrement() == 'n' && document.getCharAndIncrement() == 'f')
				{
					TomlFloat symbol = new TomlFloat(builder.toString().equals("-") ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY);
					symbols.push(symbol);
					return symbol;
				}

				document.rewindToPin();
				return null;
			}

			// NaN literal
			if (document.getCharAtPointer() == 'n')
			{
				document.incrementPointer();
				if (document.getCharAndIncrement() == 'a' && document.getCharAndIncrement() == 'n')
				{
					TomlFloat symbol = new TomlFloat(Float.NaN);
					symbols.push(symbol);

					if (builder.toString().equals("-"))
					{
						if (decoder.isWarningEnabled(TomlWarning.UNSUPPORTED_NEGATIVE_NaN) && !warned)
						{
							decoder.sendWarning(TomlWarning.UNSUPPORTED_NEGATIVE_NaN, "TOML warning: Java does not support the -NaN literal. Using +NaN instead.");
							warned = true;
						}
					}

					return symbol;
				}

				document.rewindToPin();
				return null;
			}

			// Malformed prefixes still fall back to the integer parser's validation.
			boolean integerStart = document.getCharAtPointer() >= '1' && document.getCharAtPointer() <= '9';
			boolean isExponential = false;
			boolean dotPresent = false;
			boolean isLeading = true;
			char current;
			while (document.hasNext())
			{
				current = document.getCharAtPointer();

				// Exponential format
				if (current == 'e' || current == 'E')
				{
					if (isExponential)
					{
						throw TomlSyntaxException.of("Only one 'e' exponential mark can be used in a float literal", document);
					}

					document.incrementPointer();
					builder.append(current);
					isExponential = true;
					if (document.getCharAtPointer() == '-' || document.getCharAtPointer() == '+')
					{
						builder.append(document.getCharAndIncrement());
					}

					if (document.getCharAtPointer() < '0' || document.getCharAtPointer() > '9')
					{
						throw TomlSyntaxException.of("Exponential marks must be followed by a '+', '-', or a digit", document);
					}
					continue;
				}

				if (current == '.')
				{
					if (isExponential)
					{
						throw TomlSyntaxException.of("Dot mark '.' can not be used in a float literal's exponent", document);
					}

					if (dotPresent)
					{
						throw TomlSyntaxException.of("Only one '.' dot mark can be used in a float literal", document);
					}

					if (isLeading)
					{
						throw TomlSyntaxException.of("Float literal may not have a leading '.' dot mark", document);
					}

					builder.append(current);
					document.incrementPointer();

					if (document.getCharAtPointer() < '0' || document.getCharAtPointer() > '9')
					{
						throw TomlSyntaxException.of("Dot marks in float literals must be surrounded by digits", document);
					}

					dotPresent = true;
					continue;
				}

				if (current == '_')
				{
					document.incrementPointer();
					current = document.getCharAtPointer();

					if (current < '0' || current > '9')
					{
						throw TomlSyntaxException.of("Underscore must be followed by digit, found " + current, document);
					}

					builder.append(document.getCharAndIncrement());
					continue;
				}

				if (current == ',' || current == ']' || current == '}' || current == '#' || Character.isWhitespace(current))
				{
					break;
				}

				if (current < '0' || current > '9')
				{
					document.rewindToPin();
					return null;
				}

				if (isLeading && current == '0')
				{
					// Check if we're dealing with a decimal 0 literal
					document.pinPointer().incrementPointer();
					if (document.getCharAtPointer() == ',' || document.getCharAtPointer() == 'x' || document.getCharAtPointer() == 'o' || document.getCharAtPointer() == 'b' || document.getCharAtPointer() == ']' || document.getCharAtPointer() == '}' || document.getCharAtPointer() == '#' || Character.isWhitespace(document.getCharAtPointer()))
					{
						document.rewindToPin();
						document.rewindToPin();
						return null;
					}

					if (document.getCharAtPointer() != '.' && document.getCharAtPointer() != 'e' && document.getCharAtPointer() != 'E')
					{
						document.rewindToPin();
						throw TomlSyntaxException.of("Float literal may not have any leading '0's", document);
					}
					document.rewindToPin();
				}

				isLeading = false;
				document.incrementPointer();
				builder.append(current);
			}

			if (!dotPresent && !isExponential)
			{
				if (allowInteger && integerStart)
				{
					TomlDecimal symbol = new TomlDecimal(Long.parseLong(builder.toString()));
					symbols.push(symbol);
					return symbol;
				}
				document.rewindToPin();
				return null;
			}

			TomlFloat symbol = new TomlFloat(Float.parseFloat(builder.toString()));
			symbols.push(symbol);
			return symbol;
		} finally {
			document.discardPinsAfter(pinDepth);
		}
	}
}
