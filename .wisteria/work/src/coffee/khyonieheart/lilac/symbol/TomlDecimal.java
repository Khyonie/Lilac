package coffee.khyonieheart.lilac.symbol;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.symbol.key.TomlBareKey;
import coffee.khyonieheart.lilac.symbol.key.TomlLiteralKey;
import coffee.khyonieheart.lilac.symbol.key.TomlQuotedKey;

public class TomlDecimal extends Symbol<Long>
{
	private long value;
	private static final Collection<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlComment.class, TomlStartArrayTable.class, TomlStartTable.class, TomlArraySeparator.class, TomlEndInlineTable.class, TomlEndArray.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class, TomlEndOfDocument.class);
	private static final Collection<Class<? extends Symbol<?>>> INLINE_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndInlineTable.class);
	private static final Collection<Class<? extends Symbol<?>>> ARRAY_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndArray.class);

	private static final int BASE_HEXADECIMAL = 16;
	private static final int BASE_DECIMAL     = 10;
	private static final int BASE_OCTAL       = 8;
	private static final int BASE_BINARY      = 2;

	private static final char NULL_PREFIX   = '\0';
	private static final char POSITIVE_SIGN = '+';
	private static final char NEGATIVE_SIGN = '-';
	
	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		char current = document.pinPointer().getCharAtPointer();
		char prefix = NULL_PREFIX;
		int numberBase = BASE_DECIMAL;

		StringBuilder builder = new StringBuilder();

		// Plus or minus sign
		switch (current)
		{
			case POSITIVE_SIGN -> {
				current = document.incrementPointer().getCharAtPointer();
				prefix = POSITIVE_SIGN;
			}
			case NEGATIVE_SIGN -> {
				current = document.incrementPointer().getCharAtPointer();
				prefix = NEGATIVE_SIGN;
			}
		}

		// Check for zero literal or base prefix
		if (current == '0')
		{
			current = document.pinPointer()
				.incrementPointer()
				.getCharAtPointer();

			// In case we're reading a float or time or something
			if (current == '.' || current == '-' || current == ':')
			{
				document.rewindToPin();
				return false;
			}
			
			// Valid endings for a zero literal
			if (!document.hasNext() || current == ',' || current == ']' || current == '}' || Character.isWhitespace(current))
			{
				this.value = 0;
				symbols.push(this);
				return true;
			}

			// Valid base prefixes. As for why uppercase base prefixes aren't allowed, ask the cretins who made this specification. YAML allows it. YAML! **YAML!**
			numberBase = switch (current) {
				case 'x' -> BASE_HEXADECIMAL;
				case 'o' -> BASE_OCTAL;
				case 'b' -> BASE_BINARY;
				default -> {
					if (current >= '0' && current <= '9')
					{
						throw TomlSyntaxException.of("Leading zeroes are not allowed", document);
					}

					throw TomlSyntaxException.of("Illegal character in number literal, expected a base prefix [ 'x', 'o', 'b' ]", document);
				}
			};

			if (prefix != NULL_PREFIX)
			{
				throw TomlSyntaxException.of("Numbers with a base prefix cannot be prefixed with '+' or '-'", document);
			}

			// Remove the pin
			document.incrementPointer()
				.removePin();
		}

		// Make sure the value starts with a valid number
		if (!isValidWithinBase(document.getCharAtPointer(), numberBase))
		{
			//throw TomlSyntaxException.of("Illegal character '" + document.getCharAtPointer() + "' in number literal, expected any character valid for a base " + numberBase + " literal", document);
			document.rewindToPin();
			return false;
		}

		// Read number
		while (document.hasNext())
		{
			current = document.getCharAtPointer();

			// Valid endings
			if (current == ',' || current == ']' || current == '}' || current == '#' || Character.isWhitespace(current))
			{
				break;
			}

			// In case we're reading a float or time or something
			if (current == '.' || current == '-' || current == ':')
			{
				document.rewindToPin();
				return false;
			}

			// Underscore separator
			if (current == '_')
			{
				current = document.incrementPointer().getCharAtPointer();
				
				if (!isValidWithinBase(current, numberBase))
				{
					throw TomlSyntaxException.of("Underscore separator in number literal must be surrounded with valid digits", document);
				}

				builder.append(document.getCharAndIncrement());
				continue;
			}

			if (!isValidWithinBase(current, numberBase))
			{
				throw TomlSyntaxException.of("Illegal character '" + current + "' in number literal, expected any character valid for a base " + numberBase + " literal", document);
			}

			builder.append(current);
			document.incrementPointer();
		}

		// Insert ± prefix if applicable
		if (prefix != NULL_PREFIX)
		{
			builder.insert(0, prefix);
		}

		this.value = Long.parseLong(builder.toString(), numberBase);
		symbols.push(this);

		return true;
	}

	@Override
	public Long getValue() 
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

	/**
	 * Checks if a char value is a valid number component.
	 */
	private static boolean isValidWithinBase(
		char value,
		int base
	) {
		return switch (base) {
			case BASE_HEXADECIMAL -> {
				yield (value >= '0' && value <= '9') // Decimal
					|| (value >= 'a' && value <= 'f') // Lowercase hex
					|| (value >= 'A' && value <= 'F'); // Uppercase hex
			}
			case BASE_DECIMAL -> {
				yield (value >= '0' && value <= '9');
			}
			case BASE_OCTAL -> {
				yield (value >= '0' && value <= '7');
			}
			case BASE_BINARY -> value == '0' || value == '1';
			default -> throw new IllegalStateException();
		};
	}
}
