package coffee.khyonieheart.lilac.symbol;

import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.util.SymbolParseFunction;

/**
 * Shared symbol metadata and parsing functions.
 *
 * @param types Symbol classes used for diagnostics, in parsing order.
 * @param eligibilityCheck Cheap, non-mutating check of the document's current
 * position. A false result skips parsing; true permits parsing and further validation.
 * @param parseFunction Function that parses the symbol.
 */
public record SymbolParser(
	List<Class<? extends Symbol<?>>> types,
	Predicate<Document> eligibilityCheck,
	SymbolParseFunction parseFunction
) {
	public SymbolParser
	{
		types = List.copyOf(types);
		if (types.isEmpty())
		{
			throw new IllegalArgumentException("A parser must have at least one symbol type");
		}
		Objects.requireNonNull(eligibilityCheck);
		Objects.requireNonNull(parseFunction);
	}

	public SymbolParser(Class<? extends Symbol<?>> type, Predicate<Document> eligibilityCheck, SymbolParseFunction parseFunction)
	{
		this(List.of(type), eligibilityCheck, parseFunction);
	}

	/** Creates a parser with no eligibility restrictions. */
	public SymbolParser(Class<? extends Symbol<?>> type, SymbolParseFunction parseFunction)
	{
		this(type, document -> true, parseFunction);
	}

	/** Creates a shared parser with no eligibility restrictions. */
	public SymbolParser(List<Class<? extends Symbol<?>>> types, SymbolParseFunction parseFunction)
	{
		this(types, document -> true, parseFunction);
	}

	/** Returns the first symbol type; shared parsers expose all types through {@link #types()}. */
	public Class<? extends Symbol<?>> type()
	{
		return types.get(0);
	}

	/**
	 * Parses eligible input, returning the symbol or null if unmatched.
	 * Callers must check {@link #isApplicable(Document)} first.
	 */
	public Symbol<?> tryParse(Document document, TomlDecoder decoder, Deque<Symbol<?>> symbols)
	{
		return parseFunction.apply(document, decoder, symbols);
	}

	public boolean isApplicable(Document document)
	{
		return this.eligibilityCheck.test(document);
	}
}
