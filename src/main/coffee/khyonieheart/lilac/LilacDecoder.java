/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

import coffee.khyonieheart.lilac.decoder.LilacParser;
import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.TomlEndOfDocument;
import coffee.khyonieheart.lilac.symbol.TomlStartOfDocument;
import coffee.khyonieheart.lilac.timing.StageTimer;
import coffee.khyonieheart.lilac.symbol.SymbolParser;

/**
 * Default TOML decoder implementation.
 */
public class LilacDecoder implements TomlDecoder
{
	private Deque<ParserContext> context = new ArrayDeque<>();
	private boolean[] warnings = new boolean[TomlWarning.values().length];
	private BiConsumer<TomlWarning, String> warningHandler = null;
	private TomlVersion version;

	public StageTimer timer;

	/**
	 * Creates a decoder for the given TOML specification version.
	 *
	 * @param version TOML specification version to follow while decoding.
	 */
	public LilacDecoder(
		TomlVersion version
	) {
		this.version = Objects.requireNonNull(version);
		this.enableWarnings(TomlWarning.values());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> decode(Document document) 
	{
		context.clear();

		this.timer = new StageTimer();

		Symbol<?> documentStart = new TomlStartOfDocument();
		Deque<Symbol<?>> symbols = new ArrayDeque<>();
		symbols.push(documentStart);

		if (!document.hasNext())
		{
			symbols.push(new TomlEndOfDocument());
		} else {
			timer.setStart();
			Symbol.tryParseIterative(document, this, symbols);
			timer.setAfterScan();
		}

		if (symbols.peek().getClass() != TomlEndOfDocument.class)
		{
			throwUnexpectedSymbol(document, symbols.peek());
		}

		Map<String, Object> parsed = new LilacParser(document, symbols, timer).parse();
		timer.setAfterInterpretation();

		return parsed;
	}

	private void throwUnexpectedSymbol(
		Document document,
		Symbol<?> symbol
	) {
		StringJoiner builder = new StringJoiner(", ");
		for (SymbolParser parser : symbol.getNextSymbols(this))
		{
			for (Class<? extends Symbol<?>> type : parser.types())
			{
				builder.add(type.getSimpleName());
			}
		}

		while (document.getPointer() >= document.getDocument().length())
		{
			document.hold();
		}

		throw TomlSyntaxException.of("Expected one of [ " + builder.toString() + " ], found \"" + document.getCharAtPointer() + "\"", document);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addContext(ParserContext context) 
	{
		this.context.push(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ParserContext getContext() 
	{
		return this.context.peek();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeContext() 
	{
		this.context.pop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TomlDecoder disableWarnings(TomlWarning... warnings) 
	{
		for (TomlWarning lint : warnings)
		{
			this.warnings[lint.ordinal()] = false;
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TomlDecoder enableWarnings(TomlWarning... warnings) 
	{
		for (TomlWarning lint : warnings)
		{
			this.warnings[lint.ordinal()] = true;
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWarningEnabled(TomlWarning warningType) 
	{
		Objects.requireNonNull(warningType);

		return this.warnings[warningType.ordinal()];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TomlDecoder setTomlVersion(TomlVersion version) 
	{
		this.version = Objects.requireNonNull(version);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TomlVersion getVersion() 
	{
		return this.version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LilacDecoder setWarningHandler(BiConsumer<TomlWarning, String> handler) 
	{
		this.warningHandler = handler;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
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
