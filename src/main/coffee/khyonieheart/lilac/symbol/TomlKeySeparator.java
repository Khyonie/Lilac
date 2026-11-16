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

public class TomlKeySeparator extends Symbol<String>
{
	private static final List<SymbolParser> NEXT_SYMBOLS = List.of(SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY);

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		if (document.getCharAtPointer() != '.')
		{
			return null;
		}

		TomlKeySeparator symbol = new TomlKeySeparator();
		symbols.push(symbol);
		document.incrementPointer();
		return symbol;
	}

	@Override
	public String getValue() 
	{
		return "<Key separator>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.KEY_SEPARATOR;
	}

	@Override
	public Collection<SymbolParser> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}
}
