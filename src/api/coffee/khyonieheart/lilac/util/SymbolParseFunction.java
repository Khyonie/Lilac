package coffee.khyonieheart.lilac.util;

import java.util.Deque;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.symbol.Symbol;

@FunctionalInterface
public interface SymbolParseFunction
{
	public Symbol<?> apply(
		Document document,
		TomlDecoder decoder,
		Deque<Symbol<?>> symbols
	);
}
