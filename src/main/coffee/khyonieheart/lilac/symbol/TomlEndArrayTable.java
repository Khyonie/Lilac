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

public class TomlEndArrayTable extends Symbol<String>
{
	private static final List<SymbolParser> NEXT_SYMBOLS = TomlStartOfDocument.NEXT_SYMBOLS;

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		document.incrementPointer().incrementPointer();
		TomlEndArrayTable symbol = new TomlEndArrayTable();
		symbols.push(symbol);
		return symbol;
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
	public Collection<SymbolParser> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}
}
