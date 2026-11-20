/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol;

import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;

public class TomlBoolean extends Symbol<Boolean>
{
	private boolean value;
	private static final List<SymbolParser> NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.START_ARRAY_TABLE, SymbolParsers.START_TABLE, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_INLINE_TABLE, SymbolParsers.END_ARRAY, SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY, SymbolParsers.END_OF_DOCUMENT);
	private static final List<SymbolParser> INLINE_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_INLINE_TABLE);
	private static final List<SymbolParser> ARRAY_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_ARRAY);

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		// Check literal
		switch (document.getCharAtPointer())
		{
			case 't' -> {
				document.incrementPointer();
				if (document.hasNext() && document.getCharAndIncrement() == 'r' && document.getCharAndIncrement() == 'u' && document.getCharAndIncrement() == 'e')
				{
					TomlBoolean symbol = new TomlBoolean();
					symbol.value = true;
					symbols.push(symbol);
					return symbol;
				}

				throw TomlSyntaxException.of("Invalid character in boolean literal", document);
			}
			case 'f' -> {
				document.incrementPointer();
				if (document.hasNext() && document.getCharAndIncrement() == 'a' && document.getCharAndIncrement() == 'l' && document.getCharAndIncrement() == 's' && document.getCharAndIncrement() == 'e')
				{
					TomlBoolean symbol = new TomlBoolean();
					symbol.value = false;
					symbols.push(symbol);
					return symbol;
				}

				throw TomlSyntaxException.of("Invalid character in boolean literal", document);
			}
			default -> { return null; }
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
}
