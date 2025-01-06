package coffee.khyonieheart.lilac.symbol;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;

public abstract class Symbol<T>
{
	private int position = Integer.MIN_VALUE;

	public abstract boolean tryParse(
		Document document,
		TomlDecoder decoder,
		Deque<Symbol<?>> symbols
	);

	public abstract T getValue();

	public abstract SymbolType getType();

	public int getPosition()
	{
		return this.position;
	}

	public void setPosition(
		int position
	) {
		this.position = position;
	}

	public abstract Collection<Class<? extends Symbol<?>>> getNextSymbols(
		TomlDecoder decoder
	);

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
