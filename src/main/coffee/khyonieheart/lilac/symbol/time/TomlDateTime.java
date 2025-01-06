/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.LilacTimes;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolType;
import coffee.khyonieheart.lilac.symbol.TomlArraySeparator;
import coffee.khyonieheart.lilac.symbol.TomlComment;
import coffee.khyonieheart.lilac.symbol.TomlEndArray;
import coffee.khyonieheart.lilac.symbol.TomlEndInlineTable;
import coffee.khyonieheart.lilac.symbol.TomlEndOfDocument;
import coffee.khyonieheart.lilac.symbol.TomlStartArrayTable;
import coffee.khyonieheart.lilac.symbol.TomlStartTable;
import coffee.khyonieheart.lilac.symbol.key.TomlBareKey;
import coffee.khyonieheart.lilac.symbol.key.TomlLiteralKey;
import coffee.khyonieheart.lilac.symbol.key.TomlQuotedKey;

public class TomlDateTime extends Symbol<LocalDateTime>
{
	private LocalDateTime value;
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlComment.class, TomlStartArrayTable.class, TomlStartTable.class, TomlArraySeparator.class, TomlEndInlineTable.class, TomlEndArray.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class, TomlEndOfDocument.class);
	private static final Collection<Class<? extends Symbol<?>>> INLINE_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndInlineTable.class);
	private static final Collection<Class<? extends Symbol<?>>> ARRAY_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndArray.class);

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		document.pinPointer();
		LocalDate date = LilacTimes.tryDate(document);
		if (date == null)
		{
			return false;
		}

		char current = document.getCharAndIncrement();
		if (current != ' ' && current != 't' && current != 'T')
		{
			document.rewindToPin();
			return false;
		}

		LocalTime time = LilacTimes.tryTime(document, decoder);
		if (time == null)
		{
			document.rewindToPin();
			return false;
		}

		symbols.push(this);
		this.value = LocalDateTime.of(date, time);
		return true;
	}

	@Override
	public LocalDateTime getValue() 
	{
		return this.value;
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.VALUE;
	}

	@Override
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
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
