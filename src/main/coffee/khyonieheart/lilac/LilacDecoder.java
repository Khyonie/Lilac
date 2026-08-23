/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.TomlEndOfDocument;
import coffee.khyonieheart.lilac.symbol.TomlStartOfDocument;

public class LilacDecoder implements TomlDecoder
{
	private Deque<ParserContext> context = new ArrayDeque<>();
	private boolean[] warnings = new boolean[TomlWarning.values().length];
	private BiConsumer<TomlWarning, String> warningHandler = null;
	private TomlVersion version;

	public LilacDecoder(
		TomlVersion version
	) {
		this.version = Objects.requireNonNull(version);
		this.enableWarnings(TomlWarning.values());
	}

	@Override
	public Map<String, Object> decode(Document document) 
	{
		context.clear();

		Symbol<?> documentStart = new TomlStartOfDocument();
		Deque<Symbol<?>> symbols = new ArrayDeque<>();
		symbols.push(documentStart);

		if (!document.hasNext())
		{
			symbols.push(new TomlEndOfDocument());
		} else {
			documentStart.tryParseRecursive(document, this, symbols);
		}

		if (symbols.peek().getClass() != TomlEndOfDocument.class)
		{
			throwUnexpectedSymbol(document, symbols.peek());
		}

		return new LilacParser(document, symbols).parse();
	}

	private void throwUnexpectedSymbol(
		Document document,
		Symbol<?> symbol
	) {
		StringBuilder builder = new StringBuilder();
		Iterator<Class<? extends Symbol<?>>> iter = symbol.getNextSymbols(this).iterator();
		while (iter.hasNext())
		{
			builder.append(Symbol.getSymbol(iter.next()).getClass().getSimpleName());

			if (iter.hasNext())
			{
				builder.append(", ");
			}
		}

		while (document.getPointer() >= document.getDocument().length())
		{
			document.hold();
		}

		throw TomlSyntaxException.of("Expected one of [ " + builder.toString() + " ], found \"" + document.getCharAtPointer() + "\"", document);
	}

	@Override
	public void addContext(ParserContext context) 
	{
		this.context.push(context);
	}

	@Override
	public ParserContext getContext() 
	{
		return this.context.peek();
	}

	@Override
	public void removeContext() 
	{
		this.context.pop();
	}

	@Override
	public TomlDecoder disableWarnings(TomlWarning... warnings) 
	{
		for (TomlWarning lint : warnings)
		{
			this.warnings[lint.ordinal()] = false;
		}

		return this;
	}

	@Override
	public TomlDecoder enableWarnings(TomlWarning... warnings) 
	{
		for (TomlWarning lint : warnings)
		{
			this.warnings[lint.ordinal()] = true;
		}

		return this;
	}

	@Override
	public boolean isWarningEnabled(TomlWarning warningType) 
	{
		Objects.requireNonNull(warningType);

		return this.warnings[warningType.ordinal()];
	}

	@Override
	public TomlDecoder setTomlVersion(TomlVersion version) 
	{
		this.version = Objects.requireNonNull(version);

		return this;
	}

	@Override
	public TomlVersion getVersion() 
	{
		return this.version;
	}

	@Override
	public LilacDecoder setWarningHandler(BiConsumer<TomlWarning, String> handler) 
	{
		this.warningHandler = handler;

		return this;
	}

	@Override
	public void sendWarning(TomlWarning warning, String message) 
	{
		Objects.requireNonNull(warning);
		Objects.requireNonNull(message);

		if (this.warningHandler != null)
		{
			this.warningHandler.accept(warning, message);
		}
	}
}
