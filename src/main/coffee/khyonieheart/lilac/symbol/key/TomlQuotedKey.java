/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol.key;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.LilacStrings;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolParsers;
import coffee.khyonieheart.lilac.symbol.SymbolType;
import coffee.khyonieheart.lilac.util.SymbolParseFunction;

public class TomlQuotedKey extends Symbol<String>
{
	private String value;
	private static final List<SymbolParseFunction> NEXT_SYMBOLS = List.of(SymbolParsers.KEY_SEPARATOR, SymbolParsers.EQUALS, SymbolParsers.END_ARRAY_TABLE, SymbolParsers.END_TABLE);

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		String string = LilacStrings.tryParseQuotedString(document, decoder);

		if (string == null)
		{
			return null;
		}

		TomlQuotedKey symbol = new TomlQuotedKey();
		symbol.value = string;
		symbols.push(symbol);
		return symbol;
	}

	@Override
	public String getValue() 
	{
		return this.value;
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.KEY;
	}

	@Override
	public Collection<SymbolParseFunction> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}
}
