/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol.key;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolParsers;
import coffee.khyonieheart.lilac.symbol.SymbolType;
import coffee.khyonieheart.lilac.symbol.SymbolParser;

public class TomlBareKey extends Symbol<String>
{
	private String value;
	private static final List<SymbolParser> NEXT_SYMBOLS = List.of(SymbolParsers.KEY_SEPARATOR, SymbolParsers.EQUALS, SymbolParsers.END_ARRAY_TABLE, SymbolParsers.END_TABLE);

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		int pinDepth = document.getPinDepth();
		try {
			if (document.skipToNextImportant())
			{
				throw TomlSyntaxException.of("Unexpected end of document", document);
			}

			switch (symbols.peek().getType())
			{
				case TABLE_END -> throw TomlSyntaxException.of("Expected newline between table definition and key", document);
				default -> {}
			}

			document.pinPointer();
			StringBuilder builder = new StringBuilder();
			do {
				char current = document.getCharAtPointer();
				if (current == '.' || current == ' ' || current == ']' || current == '=' || current == '#') // String-ending characters
				{
					break;
				}

				if ((current >= '0' && current <= '9') || (current >= 'A' && current <= 'Z') || (current >= 'a' && current <= 'z') || current == '-' || current == '_')
				{
					builder.append(current);
					document.incrementPointer();
					continue;
				}

				if (document.getPin() == document.getPointer())
				{
					return null;
				}

				throw TomlSyntaxException.of("Illegal bare key character \"" + current + "\"", document);
			} while (document.hasNext());

			if (builder.length() == 0)
			{
				document.rewindToPin();
				throw TomlSyntaxException.of("Key cannot be empty", document);
			}

			TomlBareKey symbol = new TomlBareKey();
			symbol.value = builder.toString();
			symbols.push(symbol);
			return symbol;
		} finally {
			document.discardPinsAfter(pinDepth);
		}
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
	public Collection<SymbolParser> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}

	public static void test() {}
}
