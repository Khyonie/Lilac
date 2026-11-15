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
import coffee.khyonieheart.lilac.util.SymbolParseFunction;

public class TomlEndArrayTable extends Symbol<String>
{
	private static final List<SymbolParseFunction> NEXT_SYMBOLS = TomlStartOfDocument.NEXT_SYMBOLS;

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		int pinDepth = document.getPinDepth();
		try {
			document.pinPointer();
			if (document.getCharAndIncrement() == ']')
			{
				if (document.getCharAndIncrement() == ']')
				{
					TomlEndArrayTable symbol = new TomlEndArrayTable();
					symbols.push(symbol);
					return symbol;
				}
			}

			document.rewindToPin();
			return null;
		} finally {
			document.discardPinsAfter(pinDepth);
		}
	}

	@Override
	public String getValue() 
	{
		return "<End of array of tables>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.TABLE_ARRAY_END;
	}

	@Override
	public Collection<SymbolParseFunction> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}
}
