package coffee.khyonieheart.lilac.symbol;

import java.util.Deque;
import java.util.Objects;
import java.util.function.Predicate;

import coffee.khyonieheart.lilac.Document;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.util.SymbolParseFunction;

/**
 * Shared symbol metadata and parsing functions.
 *
 * @param type Symbol class used for diagnostics.
 * @param eligibilityCheck Cheap, non-mutating check of the document's current
 * position. A false result skips parsing; true permits parsing and further validation.
 * @param parseFunction Function that parses the symbol.
 */
public record SymbolParser(
	Class<? extends Symbol<?>> type,
	Predicate<Document> eligibilityCheck,
	SymbolParseFunction parseFunction
) {
	public SymbolParser
	{
		Objects.requireNonNull(type);
		Objects.requireNonNull(eligibilityCheck);
		Objects.requireNonNull(parseFunction);
	}

	/** Creates a parser with no eligibility restrictions. */
	public SymbolParser(Class<? extends Symbol<?>> type, SymbolParseFunction parseFunction)
	{
		this(type, document -> true, parseFunction);
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
