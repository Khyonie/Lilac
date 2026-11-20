package coffee.khyonieheart.lilac;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.List;

import org.junit.jupiter.api.Test;

import coffee.khyonieheart.lilac.symbol.Symbol;
import coffee.khyonieheart.lilac.symbol.SymbolParsers;
import coffee.khyonieheart.lilac.symbol.TomlDecimal;
import coffee.khyonieheart.lilac.symbol.TomlFloat;

class NumberParserTest
{
	private record Literal(String text, Number value) {}

	@Test
	void sharedReaderPreservesNumericTypesAndCallerPins()
	{
		List<Literal> literals = List.of(
			new Literal("123_456", 123456L), new Literal("+42", 42L), new Literal("-42", -42L),
			new Literal("9223372036854775807", Long.MAX_VALUE), new Literal("-9223372036854775808", Long.MIN_VALUE),
			new Literal("0", 0L), new Literal("-0", 0L), new Literal("+0", 0L),
			new Literal("0xFF", 255L), new Literal("0o755", 493L), new Literal("0b101", 5L),
			new Literal("1.25", 1.25f), new Literal("-0.0", -0.0f), new Literal("1e2", 100.0f),
			new Literal("+1_000.5e-2", 10.005f), new Literal("inf", Float.POSITIVE_INFINITY),
			new Literal("-inf", Float.NEGATIVE_INFINITY), new Literal("nan", Float.NaN)
		);
		for (Literal literal : literals)
		{
			Document document = new Document(" " + literal.text() + " ");
			document.pinPointer().incrementPointer();
			var symbols = new ArrayDeque<Symbol<?>>();
			Symbol<?> result = SymbolParsers.NUMBER.tryParse(document, new LilacDecoder(TomlVersion.V1_1_0), symbols);

			assertNotNull(result, literal.text());
			assertEquals(literal.value(), result.getValue(), literal.text());
			assertEquals(literal.value() instanceof Long ? TomlDecimal.class : TomlFloat.class, result.getClass());
			assertSame(result, symbols.peek());
			assertEquals(1, symbols.size());
			assertEquals(literal.text().length() + 1, document.getPointer());
			assertEquals(1, document.getPinDepth());
			assertEquals(0, document.getPin());
		}
	}

	@Test
	void ordinaryIntegersDoNotRewindForAnotherParse()
	{
		for (String input : List.of("123456", "+123_456", "-123_456"))
		{
			Document document = new Document(input + " ") {
				@Override
				public void rewindToPin()
				{
					fail("Ordinary integers should be read without rewinding");
				}
			};
			Symbol<?> result = SymbolParsers.NUMBER.tryParse(document, new LilacDecoder(TomlVersion.V1_1_0), new ArrayDeque<>());
			assertInstanceOf(TomlDecimal.class, result);
			assertEquals(input.length(), document.getPointer());
			assertEquals(0, document.getPinDepth());
		}
	}

	@Test
	void malformedNumbersKeepExistingDiagnosticsAndPinState()
	{
		for (String input : List.of("01", "1__2", "1_", "1e+", "1.2.3", "1e2.3", "0x_", "+0xFF", "0b102", "9223372036854775808", "-9223372036854775809", "+_1", "1x", "1:2"))
		{
			Document original = new Document(" " + input + " ");
			Document shared = new Document(" " + input + " ");
			original.pinPointer().incrementPointer();
			shared.pinPointer().incrementPointer();
			var decoder = new LilacDecoder(TomlVersion.V1_1_0);
			var originalSymbols = new ArrayDeque<Symbol<?>>();
			var sharedSymbols = new ArrayDeque<Symbol<?>>();
			RuntimeException originalError = null;
			try {
				Symbol<?> result = TomlFloat.tryParse(original, decoder, originalSymbols);
				if (result == null)
				{
					result = TomlDecimal.tryParse(original, decoder, originalSymbols);
				}
				assertNull(result, input);
			} catch (RuntimeException expected) {
				originalError = expected;
			}
			if (originalError == null)
			{
				assertNull(SymbolParsers.NUMBER.tryParse(shared, decoder, sharedSymbols), input);
			} else {
				RuntimeException actual = assertThrows(RuntimeException.class,
					() -> SymbolParsers.NUMBER.tryParse(shared, decoder, sharedSymbols), input);
				assertEquals(originalError.getClass(), actual.getClass(), input);
				assertEquals(originalError.getMessage(), actual.getMessage(), input);
			}
			assertEquals(original.getPointer(), shared.getPointer(), input);
			assertEquals(1, shared.getPinDepth(), input);
			assertEquals(0, shared.getPin(), input);
			assertTrue(sharedSymbols.isEmpty(), input);
		}
	}
}
