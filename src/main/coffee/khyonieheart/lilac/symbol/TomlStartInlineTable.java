/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.ParserContext;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.util.SymbolParseFunction;

public class TomlStartInlineTable extends Symbol<String>
{
	private static final List<SymbolParseFunction> NEXT_SYMBOLS = List.of(SymbolParsers.END_INLINE_TABLE, SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY);

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		if (document.getCharAtPointer() != '{')
		{
			return null;
		}

		document.incrementPointer();
		document.skipToNextImportant();

		TomlStartInlineTable symbol = new TomlStartInlineTable();
		symbols.push(symbol);
		decoder.addContext(ParserContext.INLINE_TABLE);
		return symbol;
	}

	@Override
	public String getValue() 
	{
		return "<Start of inline table>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.INLINE_TABLE_START;
	}

	@Override
	public Collection<SymbolParseFunction> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}
}
