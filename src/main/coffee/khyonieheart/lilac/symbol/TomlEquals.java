/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol;

import java.util.Collection;
import java.util.Deque;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.decoder.LilacSymbols;

public class TomlEquals extends Symbol<String>
{
	private static final Collection<SymbolParser> NEXT_SYMBOLS = LilacSymbols.VALUE_SYMBOLS;

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		if (document.getCharAtPointer() != '=')
		{
			return null;
		}

		TomlEquals symbol = new TomlEquals();
		symbols.push(symbol);
		document.incrementPointer();
		return symbol;
	}

	@Override
	public String getValue() 
	{
		return "<Equals>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.EQUALS;
	}

	@Override
	public Collection<SymbolParser> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}
}
