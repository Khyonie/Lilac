/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol.string;

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

public class TomlMultilineLiteralString extends Symbol<String>
{
	private static final List<SymbolParser> NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.START_TABLE, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_INLINE_TABLE, SymbolParsers.END_ARRAY, SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY, SymbolParsers.END_OF_DOCUMENT);
	private static final Collection<SymbolParser> INLINE_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_INLINE_TABLE);
	private static final Collection<SymbolParser> ARRAY_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_ARRAY);
	private String value;

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		document.incrementPointer().incrementPointer().incrementPointer();

		StringBuilder builder = new StringBuilder();
		if (document.getCharAtPointer() == '\n')
		{
			document.incrementPointer();
		}

		while (document.hasNext())
		{
			char current = document.getCharAndIncrement();

			if (current == '\'')
			{
				int apostrophes = 1;
				while (document.hasNext() && document.getCharAtPointer() == '\'')
				{
					apostrophes++;
					document.incrementPointer();

					if (apostrophes > 5)
					{
						throw TomlSyntaxException.of("Too many apostrophes in multi-line literal TOML string", document);
					}
				}

				if (apostrophes < 3)
				{
					builder.append("'".repeat(apostrophes));
					continue;
				}

				builder.append("'".repeat(apostrophes - 3));
				break;
			}

			if (current <= '\b' || (current > '\n' && current <= '\u001F') || current == '\u007F')
			{
				throw TomlSyntaxException.of("Control characters cannot be used in TOML strings (found \\u" + Integer.toHexString((int) current).toUpperCase() + ")", document);
			}

			builder.append(current);
		}

		TomlMultilineLiteralString symbol = new TomlMultilineLiteralString();
		symbol.value = builder.toString();

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
		return SymbolType.VALUE;
	}

	@Override
	public Collection<SymbolParser> getNextSymbols(TomlDecoder decoder)
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
