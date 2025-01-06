package coffee.khyonieheart.lilac.symbol;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.TomlWarning;
import coffee.khyonieheart.lilac.symbol.key.TomlBareKey;
import coffee.khyonieheart.lilac.symbol.key.TomlLiteralKey;
import coffee.khyonieheart.lilac.symbol.key.TomlQuotedKey;

public class TomlFloat extends Symbol<Float> 
{
	private float value;
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlComment.class, TomlStartArrayTable.class, TomlStartTable.class, TomlArraySeparator.class, TomlEndInlineTable.class, TomlEndArray.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class, TomlEndOfDocument.class);
	private static final Collection<Class<? extends Symbol<?>>> INLINE_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndInlineTable.class);
	private static final Collection<Class<? extends Symbol<?>>> ARRAY_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndArray.class);
	private static boolean warned = false;

	// [+-]?(\d+).(\d+)
	//

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		if ((document.getCharAtPointer() < '0' && document.getCharAtPointer() > '9') && document.getCharAtPointer() != '-' && document.getCharAtPointer() != '+' && document.getCharAtPointer() != 'n' && document.getCharAtPointer() != 'i')
		{
			return false;
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
				symbols.push(this);
				this.value = builder.toString().equals("-") ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
				return true;
			}

			document.rewindToPin();
			return false;
		}

		// NaN literal
		if (document.getCharAtPointer() == 'n')
		{
			document.incrementPointer();
			if (document.getCharAndIncrement() == 'a' && document.getCharAndIncrement() == 'n')
			{
				symbols.push(this);

				if (builder.toString().equals("-"))
				{
					if (decoder.isWarningEnabled(TomlWarning.UNSUPPORTED_NEGATIVE_NaN) && !warned)
					{
						decoder.sendWarning(TomlWarning.UNSUPPORTED_NEGATIVE_NaN, "TOML warning: Java does not support the -NaN literal. Using +NaN instead.");
						warned = true;
					}
				}

				this.value = Float.NaN;
				return true;
			}

			document.rewindToPin();
			return false;
		}

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
				return false;
			}

			if (isLeading && current == '0')
			{
				// Check if we're dealing with a decimal 0 literal
				document.pinPointer().incrementPointer();
				if (document.getCharAtPointer() == ',' || document.getCharAtPointer() == 'x' || document.getCharAtPointer() == 'o' || document.getCharAtPointer() == 'b' || document.getCharAtPointer() == ']' || document.getCharAtPointer() == '}' || document.getCharAtPointer() == '#' || Character.isWhitespace(document.getCharAtPointer()))
				{
					document.rewindToPin();
					document.rewindToPin();
					return false;
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
			document.rewindToPin();
			return false;
		}

		this.value = Float.parseFloat(builder.toString());
		symbols.push(this);
		return true;
	}

	@Override
	public Float getValue() 
	{
		return value;
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
}
