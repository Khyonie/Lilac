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

public class TomlEndOfDocument extends Symbol<String>
{
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of();

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		if (document.hasNext())
		{
			return false;
		}

		symbols.push(this);
		return true;
	}

	@Override
	public String getValue() 
	{
		return "<End of document>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.DOCUMENT_END;
	}

	@Override
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		return NEXT_SYMBOLS;
	}
}
