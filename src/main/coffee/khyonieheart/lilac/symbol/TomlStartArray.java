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
import coffee.khyonieheart.lilac.symbol.string.TomlLiteralString;
import coffee.khyonieheart.lilac.symbol.string.TomlMultilineLiteralString;
import coffee.khyonieheart.lilac.symbol.string.TomlMultilineString;
import coffee.khyonieheart.lilac.symbol.string.TomlRegularString;
import coffee.khyonieheart.lilac.symbol.time.TomlDate;
import coffee.khyonieheart.lilac.symbol.time.TomlDateTime;
import coffee.khyonieheart.lilac.symbol.time.TomlOffsetDateTime;
import coffee.khyonieheart.lilac.symbol.time.TomlTime;

public class TomlStartArray extends Symbol<String>
{
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlComment.class, TomlStartArray.class, TomlEndArray.class, TomlStartInlineTable.class, TomlBoolean.class, TomlMultilineString.class, TomlMultilineLiteralString.class, TomlRegularString.class, TomlLiteralString.class, TomlOffsetDateTime.class, TomlDateTime.class, TomlDate.class, TomlTime.class, TomlFloat.class, TomlDecimal.class, TomlEndArray.class);

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		if (document.getCharAtPointer() != '[')
		{
			return false;
		}

		document.incrementPointer();
		document.skipToNextImportant();

		symbols.push(this);
		decoder.addContext(ParserContext.ARRAY);
		return true;
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
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		return NEXT_SYMBOLS;
	}
}
