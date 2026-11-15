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
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.TomlVersion;
import coffee.khyonieheart.lilac.TomlWarning;
import coffee.khyonieheart.lilac.util.SymbolParseFunction;

public class TomlNewline extends Symbol<String>
{
	private static final List<SymbolParseFunction> NEXT_SYMBOLS = TomlStartOfDocument.NEXT_SYMBOLS;
	private static boolean warned = false;

	private static final String NEWLINE_DISALLOWED_MESSAGE = "Newlines are disallowed at this location";

	public static Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		if (document.getCharAtPointer() != '\n')
		{
			return null;
		}
		TomlNewline symbol = new TomlNewline();
		symbol.setPosition(document.getPointer());

		switch (symbols.peek().getType())
		{
			case KEY -> throw TomlSyntaxException.of(NEWLINE_DISALLOWED_MESSAGE, document);
			case EQUALS -> throw TomlSyntaxException.of(NEWLINE_DISALLOWED_MESSAGE, document);
			case TABLE_START -> throw TomlSyntaxException.of(NEWLINE_DISALLOWED_MESSAGE, document);
			case TABLE_ARRAY_START -> throw TomlSyntaxException.of(NEWLINE_DISALLOWED_MESSAGE, document);
			case KEY_SEPARATOR -> throw TomlSyntaxException.of(NEWLINE_DISALLOWED_MESSAGE, document);
			default -> {}
		}

		document.incrementPointer();
		if (!document.hasNext())
		{
			symbols.push(new TomlEndOfDocument());
			return symbol;
		}

		switch (symbols.peek().getType())
		{
			case KEY -> throw TomlSyntaxException.of(NEWLINE_DISALLOWED_MESSAGE, document);
			default -> {}
		}

		if (decoder.getContext() != null && decoder.getContext() == ParserContext.INLINE_TABLE)
		{
			if (decoder.getVersion() == TomlVersion.V1_0_0)
			{
				throw TomlSyntaxException.of("Inline tables cannot be broken into multiple lines", document);
			}

			if (decoder.isWarningEnabled(TomlWarning.MULTILINE_INLINE_TABLE) && !warned)
			{
				decoder.sendWarning(TomlWarning.MULTILINE_INLINE_TABLE, "TOML warning: breaking an inline table into multiple lines is discouraged. Consider using a regular table instead.");
				warned = true;
			}
		}

		symbols.push(symbol);
		return symbol;
	}

	@Override
	public String getValue() 
	{
		return "<Newline>";
	}

	@Override
	public SymbolType getType() 
	{
		return SymbolType.NEWLINE;
	}

	@Override
	public Collection<SymbolParseFunction> getNextSymbols(TomlDecoder decoder)
	{
		return NEXT_SYMBOLS;
	}
}
