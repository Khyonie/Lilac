/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.LilacSymbols;
import coffee.khyonieheart.lilac.ParserContext;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.symbol.key.TomlBareKey;
import coffee.khyonieheart.lilac.symbol.key.TomlLiteralKey;
import coffee.khyonieheart.lilac.symbol.key.TomlQuotedKey;

public class TomlComment extends Symbol<String>
{
	private String comment;
	private static final Collection<Class<? extends Symbol<?>>> STANDARD_NEXT_SYMBOLS = List.of(TomlComment.class, TomlStartArrayTable.class, TomlStartTable.class, TomlArraySeparator.class, TomlEndInlineTable.class, TomlEndArray.class, TomlQuotedKey.class, TomlLiteralKey.class, TomlBareKey.class, TomlEndOfDocument.class);
	private static final Collection<Class<? extends Symbol<?>>> ARRAY_NEXT_SYMBOLS = LilacSymbols.build().add(TomlComment.class, TomlArraySeparator.class, TomlEndArray.class).add(LilacSymbols.VALUE_SYMBOLS).create();

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		if (document.getCharAtPointer() != '#')
		{
			return false;
		}

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

		this.comment = builder.toString();
		symbols.push(this);

		if (!document.hasNext())
		{
			symbols.push(new TomlEndOfDocument());
		}
		return true;
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
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		if (decoder.getContext() == null)
		{
			return STANDARD_NEXT_SYMBOLS;
		}

		return ARRAY_NEXT_SYMBOLS;
	}
}
