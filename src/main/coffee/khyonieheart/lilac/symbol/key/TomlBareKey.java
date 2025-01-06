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
import coffee.khyonieheart.lilac.symbol.SymbolType;
import coffee.khyonieheart.lilac.symbol.TomlEndArrayTable;
import coffee.khyonieheart.lilac.symbol.TomlEndTable;
import coffee.khyonieheart.lilac.symbol.TomlEquals;
import coffee.khyonieheart.lilac.symbol.TomlKeySeparator;

public class TomlBareKey extends Symbol<String>
{
	private String value;
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = List.of(TomlKeySeparator.class, TomlEquals.class, TomlEndArrayTable.class, TomlEndTable.class);

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
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
				return false;
			}

			throw TomlSyntaxException.of("Illegal bare key character \"" + current + "\"", document);
		} while (document.hasNext());

		if (builder.isEmpty())
		{
			document.rewindToPin();
			throw TomlSyntaxException.of("Key cannot be empty", document);
		}

		this.value = builder.toString();
		symbols.push(this);
		return true;
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
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		return NEXT_SYMBOLS;
	}
}
