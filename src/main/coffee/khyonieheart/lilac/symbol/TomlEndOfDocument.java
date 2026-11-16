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
	private static final List<SymbolParser> NEXT_SYMBOLS = List.of();

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		if (document.hasNext())
		{
			return null;
		}

		TomlEndOfDocument symbol = new TomlEndOfDocument();
		symbols.push(symbol);
		return symbol;
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
	public Collection<SymbolParser> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}
}
