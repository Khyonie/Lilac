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
import coffee.khyonieheart.lilac.symbol.key.TomlBareKey;
import coffee.khyonieheart.lilac.symbol.key.TomlLiteralKey;
import coffee.khyonieheart.lilac.symbol.key.TomlQuotedKey;

public class TomlStartInlineTable extends Symbol<String>
{
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlEndInlineTable.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class);

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		if (document.getCharAtPointer() != '{')
		{
			return false;
		}

		document.incrementPointer();
		document.skipToNextImportant();

		symbols.push(this);
		decoder.addContext(ParserContext.INLINE_TABLE);
		return true;
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
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		return NEXT_SYMBOLS;
	}
}
