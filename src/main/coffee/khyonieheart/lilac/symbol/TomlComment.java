/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol;

import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.ParserContext;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.decoder.LilacSymbols;

public class TomlComment extends Symbol<String>
{
	private String comment;
	private static final List<SymbolParser> STANDARD_NEXT_SYMBOLS = List.of(SymbolParsers.COMMENT, SymbolParsers.START_ARRAY_TABLE, SymbolParsers.START_TABLE, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_INLINE_TABLE, SymbolParsers.END_ARRAY, SymbolParsers.QUOTED_KEY, SymbolParsers.LITERAL_KEY, SymbolParsers.BARE_KEY, SymbolParsers.END_OF_DOCUMENT);
	private static final List<SymbolParser> ARRAY_NEXT_SYMBOLS = LilacSymbols.build().add(SymbolParsers.COMMENT, SymbolParsers.ARRAY_SEPARATOR, SymbolParsers.END_ARRAY).add(LilacSymbols.VALUE_SYMBOLS).create();

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		if (decoder.getContext() != null)
		{
			if (decoder.getContext() == ParserContext.INLINE_TABLE)
			{
				throw TomlSyntaxException.of("Comments are not allowed inside inline tables", document);
			}
		}

		document.incrementPointer();
		document.skipToNextImportant();

		StringBuilder builder = new StringBuilder();

		char current;
		while (document.hasNext())
		{
			current = document.getCharAtPointer();
			if (current == '\n')
			{
				break;
			}

			if (current == '\r')
			{
				document.incrementPointer();
				if (document.getCharAtPointer() != '\n')
				{
					throw TomlSyntaxException.of("Line feed character in comment must be followed by a line feed (\\r\\n)", document);
				}
				break;
			}

			if (current == '\t')
			{
				document.incrementPointer();
				builder.append(current);
				continue;
			}

			if (current <= '\u0008' || (current >= (char) 0x000A && current <= '\u001F') || current == '\u007F')
			{
				throw TomlSyntaxException.of("Control characters are not allowed in comments", document);
			}

			document.incrementPointer();
			builder.append(current);
		}

		TomlComment symbol = new TomlComment();
		symbol.comment = builder.toString();
		symbols.push(symbol);

		if (!document.hasNext())
		{
			symbols.push(new TomlEndOfDocument());
		}
		return symbol;
	}

	@Override
	public String getValue() 
	{
		return this.comment;
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.COMMENT;
	}

	@Override
	public List<SymbolParser> getNextSymbols(TomlDecoder decoder)
	{
		if (decoder.getContext() == null)
		{
			return STANDARD_NEXT_SYMBOLS;
		}

		return ARRAY_NEXT_SYMBOLS;
	}
}
