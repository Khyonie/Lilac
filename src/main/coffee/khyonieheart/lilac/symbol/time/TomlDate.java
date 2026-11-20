/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol.time;

import java.time.LocalDate;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.decoder.LilacTimes;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolParsers;
import coffee.khyonieheart.lilac.symbol.SymbolType;
import coffee.khyonieheart.lilac.symbol.SymbolParser;

public class TomlDate extends Symbol<LocalDate>
{
	private LocalDate value;
	private static final List<SymbolParser> NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.START_ARRAY_TABLE, SymbolParsers.START_TABLE, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_INLINE_TABLE, SymbolParsers.END_ARRAY, SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY, SymbolParsers.END_OF_DOCUMENT);
	private static final List<SymbolParser> INLINE_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_INLINE_TABLE);
	private static final List<SymbolParser> ARRAY_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_ARRAY);

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		LocalDate date = LilacTimes.tryDate(document);
		if (date == null)
		{
			return null;
		}

		TomlDate symbol = new TomlDate();
		symbols.push(symbol);
		symbol.value = date;
		return symbol;
	}

	@Override
	public LocalDate getValue() 
	{
		return this.value;
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.VALUE;
	}

	@Override
	public List<SymbolParser> getNextSymbols(TomlDecoder decoder)
	{
		if (decoder.getContext() == null)
		{
			return NEXT_SYMBOLS;
		}

		return switch (decoder.getContext()) {
			case ARRAY -> ARRAY_NEXT_SYMBOLS;
			case INLINE_TABLE -> INLINE_NEXT_SYMBOLS;
			case ROOT -> NEXT_SYMBOLS;
		};
	}
}
