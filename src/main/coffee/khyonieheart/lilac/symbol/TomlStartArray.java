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

public class TomlStartArray extends Symbol<String>
{
	private static final List<SymbolParseFunction> NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.START_ARRAY, SymbolParsers.END_ARRAY, SymbolParsers.START_INLINE_TABLE, SymbolParsers.BOOLEAN, SymbolParsers.MULTILINE_STRING, SymbolParsers.MULTILINE_LITERAL_STRING, SymbolParsers.REGULAR_STRING, SymbolParsers.LITERAL_STRING, SymbolParsers.OFFSET_DATE_TIME, SymbolParsers.DATE_TIME, SymbolParsers.DATE, SymbolParsers.TIME, SymbolParsers.FLOAT, SymbolParsers.DECIMAL, SymbolParsers.END_ARRAY);

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		if (document.getCharAtPointer() != '[')
		{
			return null;
		}

		document.incrementPointer();
		document.skipToNextImportant();

		TomlStartArray symbol = new TomlStartArray();
		symbols.push(symbol);
		decoder.addContext(ParserContext.ARRAY);
		return symbol;
	}

	@Override
	public String getValue() 
	{
		return "<Start of array>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.ARRAY_START;
	}

	@Override
	public Collection<SymbolParseFunction> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}
}
