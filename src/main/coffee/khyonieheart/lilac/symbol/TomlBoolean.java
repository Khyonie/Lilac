/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
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

public class TomlBoolean extends Symbol<Boolean>
{
	private boolean value;
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlComment.class, TomlStartArrayTable.class, TomlStartTable.class, TomlArraySeparator.class, TomlEndInlineTable.class, TomlEndArray.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class, TomlEndOfDocument.class);
	private static final Collection<Class<? extends Symbol<?>>> INLINE_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndInlineTable.class);
	private static final Collection<Class<? extends Symbol<?>>> ARRAY_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndArray.class);

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		// Check literal
		switch (document.getCharAtPointer())
		{
			case 't' -> {
				document.incrementPointer();
				if (document.hasNext() && document.getCharAndIncrement() == 'r' && document.getCharAndIncrement() == 'u' && document.getCharAndIncrement() == 'e')
				{
					this.value = true;
					symbols.push(this);
					return true;
				}

				throw TomlSyntaxException.of("Invalid character in boolean literal", document);
			}
			case 'f' -> {
				document.incrementPointer();
				if (document.hasNext() && document.getCharAndIncrement() == 'a' && document.getCharAndIncrement() == 'l' && document.getCharAndIncrement() == 's' && document.getCharAndIncrement() == 'e')
				{
					this.value = false;
					symbols.push(this);
					return true;
				}

				throw TomlSyntaxException.of("Invalid character in boolean literal", document);
			}
			default -> { return false; }
		}
	}

	@Override
	public Boolean getValue() 
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
}
