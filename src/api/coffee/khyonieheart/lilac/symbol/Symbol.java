/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac.symbol;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;

/**
 * Parser symbol used by Lilac's recursive scanner.
 *
 * Most callers should use {@link TomlDecoder} instead of interacting with symbols directly.
 *
 * @param <T> Value type produced by this symbol.
 */
public abstract class Symbol<T>
{
	private int position = Integer.MIN_VALUE;

	/**
	 * Attempts to parse this symbol at the document's current pointer position.
	 *
	 * @param document Document being parsed.
	 * @param decoder Decoder that owns parser state.
	 * @param symbols Symbol stack being built by the scanner.
	 *
	 * @return {@code true} if this symbol parsed successfully, {@code false} otherwise.
	 */
	public abstract boolean tryParse(
		Document document,
		TomlDecoder decoder,
		Deque<Symbol<?>> symbols
	);

	/**
	 * Gets the value parsed by this symbol.
	 *
	 * @return Parsed symbol value.
	 */
	public abstract T getValue();

	/**
	 * Gets this symbol's category.
	 *
	 * @return Symbol category.
	 */
	public abstract SymbolType getType();

	/**
	 * Gets the source position where this symbol was parsed.
	 *
	 * @return Source position for this symbol.
	 */
	public int getPosition()
	{
		return this.position;
	}

	/**
	 * Sets the source position where this symbol was parsed.
	 *
	 * @param position Source position for this symbol.
	 */
	public void setPosition(
		int position
	) {
		this.position = position;
	}

	/**
	 * Gets the symbol classes that may follow this symbol in the current parser context.
	 *
	 * @param decoder Decoder that owns parser state.
	 *
	 * @return Symbol classes that may be parsed next.
	 */
	public abstract Collection<Class<? extends Symbol<?>>> getNextSymbols(
		TomlDecoder decoder
	);

	/**
	 * Creates a new instance of a symbol class.
	 *
	 * @param symbol Symbol class to instantiate.
	 *
	 * @return New symbol instance.
	 * @throws IllegalStateException Thrown if the symbol cannot be instantiated.
	 */
	public static Symbol<?> getSymbol(
		Class<? extends Symbol<?>> symbol
	) {
		try {
			Constructor<? extends Symbol<?>> constructor = symbol.getConstructor();

			return constructor.newInstance();
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			throw new IllegalStateException("Failed to create instance of symbol " + symbol.getName(), e);
		}
	}

	/**
	 * Recursively scans symbols from the current document pointer.
	 *
	 * @param document Document being parsed.
	 * @param decoder Decoder that owns parser state.
	 * @param symbols Symbol stack being built by the scanner.
	 */
	public void tryParseRecursive(
		Document document,
		TomlDecoder decoder,
		Deque<Symbol<?>> symbols
	) {
		Objects.requireNonNull(document);
		Objects.requireNonNull(decoder);
		Objects.requireNonNull(symbols);

		if (document.skipThroughWhitespace(symbols, decoder))
		{
			return;
		}

		int position = document.getPointer();
		for (Class<? extends Symbol<?>> symbolClass : this.getNextSymbols(decoder))
		{
			Symbol<?> symbol = getSymbol(symbolClass);
			if (symbol.tryParse(document, decoder, symbols))
			{
				symbol.setPosition(position);
				if (document.skipThroughWhitespace(symbols, decoder))
				{
					return;
				}
				symbol.tryParseRecursive(document, decoder, symbols);
				break;
			}
		}
	}
}
