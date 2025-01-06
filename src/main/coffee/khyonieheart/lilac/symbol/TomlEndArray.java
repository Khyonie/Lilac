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
import coffee.khyonieheart.lilac.symbol.key.TomlBareKey;
import coffee.khyonieheart.lilac.symbol.key.TomlLiteralKey;
import coffee.khyonieheart.lilac.symbol.key.TomlQuotedKey;

public class TomlEndArray extends Symbol<String>
{
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlComment.class, TomlStartArrayTable.class, TomlStartTable.class, TomlEndInlineTable.class, TomlEndArray.class, TomlArraySeparator.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class, TomlEndOfDocument.class);
	private static final Collection<Class<? extends Symbol<?>>> INLINE_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndInlineTable.class);
	private static final Collection<Class<? extends Symbol<?>>> ARRAY_NEXT_SYMBOLS = List.of(TomlComment.class, TomlArraySeparator.class, TomlEndArray.class);

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		if (document.getCharAtPointer() != ']')
		{
			return false;
		}

		document.incrementPointer();
		document.skipToNextImportant();

		symbols.push(this);
		decoder.removeContext();
		return true;
	}

	@Override
	public String getValue() 
	{
		return "<End of array>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.ARRAY_END;
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
