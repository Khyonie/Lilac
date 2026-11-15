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
import coffee.khyonieheart.lilac.TomlVersion;
import coffee.khyonieheart.lilac.util.SymbolParseFunction;

public class TomlArraySeparator extends Symbol<String>
{
	private static final List<SymbolParseFunction> ARRAY_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.START_ARRAY, SymbolParsers.END_ARRAY, SymbolParsers.START_INLINE_TABLE, SymbolParsers.BOOLEAN, SymbolParsers.MULTILINE_STRING, SymbolParsers.MULTILINE_LITERAL_STRING, SymbolParsers.REGULAR_STRING, SymbolParsers.LITERAL_STRING, SymbolParsers.OFFSET_DATE_TIME, SymbolParsers.DATE_TIME, SymbolParsers.DATE, SymbolParsers.TIME, SymbolParsers.FLOAT, SymbolParsers.DECIMAL);
	private static final List<SymbolParseFunction> INLINE_TABLE_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY);
	private static final List<SymbolParseFunction> V1_1_INLINE_TABLE_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY, SymbolParsers.END_INLINE_TABLE);

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		if (document.getCharAtPointer() != ',')
		{
			return null;
		}

		TomlArraySeparator symbol = new TomlArraySeparator();
		symbols.push(symbol);
		document.incrementPointer();
		return symbol;
	}

	@Override
	public String getValue() 
	{
		return "<Array separator>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.ARRAY_SEPARATOR;
	}

	@Override
	public Collection<SymbolParseFunction> getNextSymbols(TomlDecoder decoder)
	{
		ParserContext context = decoder.getContext();

		if (context == null)
		{
			throw new IllegalStateException("Context must not be null when getting next symbols for array");
		}

		return switch (context) {
			case ARRAY -> ARRAY_NEXT_SYMBOLS;
			case INLINE_TABLE -> decoder.getVersion() == TomlVersion.V1_0_0 ? INLINE_TABLE_NEXT_SYMBOLS : V1_1_INLINE_TABLE_NEXT_SYMBOLS;
			case ROOT -> throw new IllegalStateException("Context not valid for lexeme");
		};
	}
}
