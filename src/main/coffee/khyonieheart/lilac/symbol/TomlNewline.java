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

public class TomlNewline extends Symbol<String>
{
	private static final List<Class<? extends Symbol<?>>> NEXT_SYMBOLS = TomlStartOfDocument.NEXT_SYMBOLS;
	private static boolean warned = false;

	private static final String NEWLINE_DISALLOWED_MESSAGE = "Newlines are disallowed at this location";

	@Override
	public boolean tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols) 
	{
		if (document.getCharAtPointer() != '\n')
		{
			return false;
		}
		this.setPosition(document.getPointer());

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
			return true;
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

		symbols.push(this);
		return true;
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
	public Collection<Class<? extends Symbol<?>>> getNextSymbols(TomlDecoder decoder) 
	{
		return NEXT_SYMBOLS;
	}
}
