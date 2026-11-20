/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol;

import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;

public class TomlStartTable extends Symbol<String>
{
	private static final List<SymbolParser> NEXT_SYMBOLS = List.of(SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY);

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		document.incrementPointer();
		document.skipToNextImportant();

		TomlStartTable symbol = new TomlStartTable();
		symbols.push(symbol);
		return symbol;
	}

	@Override
	public String getValue() 
	{
		return "[";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.TABLE_START;
	}

	@Override
	public List<SymbolParser> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}
}
