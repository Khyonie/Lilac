package coffee.khyonieheart.lilac;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import coffee.khyonieheart.lilac.decoder.LilacStrings;
import coffee.khyonieheart.lilac.decoder.LilacTimes;
import coffee.khyonieheart.lilac.symbol.*;
import coffee.khyonieheart.lilac.util.SymbolParseFunction;

class PinLifetimeTest
{
	private record Parser(String name, SymbolParseFunction parse)
	{
		Parser(String name, SymbolParser parser)
		{
			this(name, (document, decoder, symbols) -> parser.isApplicable(document)
				? parser.tryParse(document, decoder, symbols) : null);
		}
	}

	private static class TrackingDocument extends Document
	{
		private int maxPinDepth;

		TrackingDocument(String text)
		{
			super(text);
		}

		@Override
		public Document pinPointer()
		{
			super.pinPointer();
			maxPinDepth = Math.max(maxPinDepth, getPinDepth());
			return this;
		}
	}

	@TestFactory
	Stream<DynamicTest> parsersPreserveCallerPinsOnEveryExit()
	{
		List<Parser> parsers = List.of(
			new Parser("bare key", SymbolParsers.BARE_KEY),
			new Parser("decimal", SymbolParsers.DECIMAL),
			new Parser("float", SymbolParsers.FLOAT),
			new Parser("array table start", SymbolParsers.START_ARRAY_TABLE),
			new Parser("array table end", SymbolParsers.END_ARRAY_TABLE),
			new Parser("multiline string", SymbolParsers.MULTILINE_STRING),
			new Parser("multiline literal", SymbolParsers.MULTILINE_LITERAL_STRING),
			new Parser("date time", SymbolParsers.DATE_TIME),
			new Parser("offset date time", SymbolParsers.OFFSET_DATE_TIME),
			new Parser("quoted helper", (d, decoder, symbols) -> { LilacStrings.tryParseQuotedString(d, decoder); return null; }),
			new Parser("literal helper", (d, decoder, symbols) -> { LilacStrings.tryParseLiteralString(d); return null; }),
			new Parser("date helper", (d, decoder, symbols) -> { LilacTimes.tryDate(d); return null; }),
			new Parser("time helper", (d, decoder, symbols) -> { LilacTimes.tryTime(d, decoder); return null; }),
			new Parser("offset helper", (d, decoder, symbols) -> { LilacTimes.tryOffset(d); return null; })
		);
		List<String> inputs = List.of(
			"", "x", "key =", "@", "[[x", "]]x", "[x", "]x",
			"0 ", "-0 ", "+0 ", "0xFF ", "0o77 ", "0b10 ", "0.5 ", "0: ", "0- ",
			"123 ", "-123 ", "1_000 ", "01 ", "0x_ ", "1__2 ", "9223372036854775808 ",
			"1.5 ", "1e2 ", "1e+ ", "1.2.3 ", "inf ", "nan ", "-nan ", "inX ",
			"\"hello\"", "'hello'", "\"\"", "''", "\"unterminated", "'unterminated", "\"a\nb\"", "\"\\q\"",
			"\"\"\"hello\"\"\"", "'''hello'''", "'''bad\u0001'''", "\"\"\"bad\\q\"\"\"",
			"\"\"\"a\\\n  b\"\"\"", "\"\"\"a\\  x\"\"\"", "\"\"\"a\\   ",
			"1979-05-27 ", "1979-02-30 ", "1979-05-27X ", "1979-05-27Tbad ",
			"1979-05-27T07:32:00 ", "1979-05-27T07:32:00Z ", "1979-05-27T07:32:00+01:00 ",
			"1979-05-27T07:32:00+99:00 ", "07:32:00 ", "07:32 ", "07:32:00.123 ",
			"24:00:00 ", "07:60:00 ", "07:32:60 ", "+01:00 ", "+99:00 ", "+01:60 ", "Z "
		);
		return parsers.stream().flatMap(parser -> inputs.stream().flatMap(input ->
			Stream.of(TomlVersion.V1_0_0, TomlVersion.V1_1_0).map(version ->
				DynamicTest.dynamicTest(parser.name() + "/" + version + "/" + input.replace("\n", "\\n"), () -> {
					Document document = new Document(" " + input);
					document.pinPointer(); // A caller's checkpoint, distinct from the parser's position.
					document.incrementPointer();
					var symbols = new ArrayDeque<Symbol<?>>();
					symbols.push(new TomlStartOfDocument());
					try {
						parser.parse().apply(document, new LilacDecoder(version), symbols);
					} catch (RuntimeException expectedForInvalidInput) {
						// Exercise cleanup even when parsing terminates with an error.
					}
					assertEquals(1, document.getPinDepth());
					assertEquals(0, document.getPin());
				})
			)));
	}

	@Test
	void invalidDateReportsSyntaxErrorWithoutConsumingCallerPin()
	{
		Document document = new Document("1979-02-30 ");
		document.pinPointer();
		assertThrows(TomlSyntaxException.class, () -> LilacTimes.tryDate(document));
		assertEquals(1, document.getPinDepth());
		assertEquals(0, document.getPin());
		assertEquals(0, document.getPointer());
	}

	@Test
	void discardingPinsPreservesPointerAndRejectsUnderflow()
	{
		Document document = new Document("abc");
		document.pinPointer().incrementPointer().pinPointer().incrementPointer();
		document.discardPinsAfter(1);
		assertEquals(2, document.getPointer());
		assertEquals(0, document.getPin());
		assertThrows(IllegalStateException.class, () -> document.discardPinsAfter(2));
		assertThrows(IllegalArgumentException.class, () -> document.discardPinsAfter(-1));
		document.rewindToPin();
		assertEquals(0, document.getPointer());
		assertEquals(0, document.getPinDepth());
	}

	@Test
	void pinDepthStaysBoundedAsDocumentsGrow()
	{
		int shortDepth = decodeRepeatedValues(100);
		int longDepth = decodeRepeatedValues(20_000);
		assertEquals(shortDepth, longDepth);
		assertTrue(longDepth <= 3, "Only active parser checkpoints should remain");
	}

	private int decodeRepeatedValues(int count)
	{
		String[] values = { "0", "0xFF", "123", "1.25", "'literal'", "\"quoted\"", "'''multiline'''",
			"\"\"\"continued\\\n  string\"\"\"", "1979-05-27", "07:32:00", "1979-05-27T07:32:00", "1979-05-27T07:32:00Z" };
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < count; i++)
		{
			text.append("key").append(i).append(" = ").append(values[i % values.length]).append('\n');
		}
		TrackingDocument document = new TrackingDocument(text.toString());
		document.pinPointer();
		assertEquals(count, new LilacDecoder(TomlVersion.V1_1_0).decode(document).size());
		assertEquals(1, document.getPinDepth());
		assertEquals(0, document.getPin());
		document.removePin();
		assertEquals(0, document.getPinDepth());
		return document.maxPinDepth;
	}
}
