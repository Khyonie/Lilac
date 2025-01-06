/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol;

import java.util.Collection;
import java.util.Deque;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.LilacSymbols;
import coffee.khyonieheart.lilac.TomlDecoder;

public class TomlEquals extends Symbol<String>
{
	private static final Collection<Class<? extends Symbol<?>>> NEXT_SYMBOLS = LilacSymbols.VALUE_SYMBOLS;

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		if (document.getCharAtPointer() != '=')
		{
			return false;
		}

		symbols.push(this);
		document.incrementPointer();
		return true;
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
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		return NEXT_SYMBOLS;
	}
}
