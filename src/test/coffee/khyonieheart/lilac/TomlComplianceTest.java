package coffee.khyonieheart.lilac;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class TomlComplianceTest
{
	@TestFactory
	Stream<DynamicTest> toml10ValidDocuments()
		throws IOException
	{
		return dynamicTests(TomlVersion.V1_0_0, TestSection.VALID);
	}

	@TestFactory
	Stream<DynamicTest> toml10InvalidDocuments()
		throws IOException
	{
		return dynamicTests(TomlVersion.V1_0_0, TestSection.INVALID);
	}

	@TestFactory
	Stream<DynamicTest> toml11ValidDocuments()
		throws IOException
	{
		return dynamicTests(TomlVersion.V1_1_0, TestSection.VALID);
	}

	@TestFactory
	Stream<DynamicTest> toml11InvalidDocuments()
		throws IOException
	{
		return dynamicTests(TomlVersion.V1_1_0, TestSection.INVALID);
	}

	private Stream<DynamicTest> dynamicTests(
		TomlVersion version,
		TestSection section
	) throws IOException {
		TomlTestIndex index = TomlTestIndex.load(version, section);
		return index.targetFiles().stream()
			.map(test -> DynamicTest.dynamicTest(version.name() + "/" + test, () -> runTest(version, section, test)));
	}

	private void runTest(
		TomlVersion version,
		TestSection section,
		String test
	) throws Exception {
		switch (section)
		{
			case VALID -> assertValidDocument(version, test);
			case INVALID -> assertInvalidDocument(version, test);
		}
	}

	private void assertValidDocument(
		TomlVersion version,
		String test
	) throws IOException {
		Map<String, Object> decoded = assertDoesNotThrow(() -> decode(version, test));
		Map<String, Object> expected = expectedDocument(test);

		assertEquals(expected, decoded, "Decoded document did not match toml-test JSON for " + test);

		String encoded = new LilacEncoder().encode(decoded);
		Map<String, Object> redecoded = new LilacDecoder(version).decode(encoded);
		assertEquals(decoded, redecoded, "Re-encoded document did not round-trip for " + test);
	}

	private void assertInvalidDocument(
		TomlVersion version,
		String test
	) {
		Throwable thrown = assertThrows(Throwable.class, () -> decode(version, test), "Invalid TOML was accepted: " + test);
		assertTrue(isExpectedInvalidFailure(thrown), "Invalid TOML failed with an unexpected exception type: " + thrown);
	}

	private Map<String, Object> decode(
		TomlVersion version,
		String test
	) throws IOException {
		String toml = Files.readString(new File("tests/" + test + ".toml").toPath());
		return new LilacDecoder(version).decode(new Document(toml));
	}

	private Map<String, Object> expectedDocument(
		String test
	) throws IOException {
		return TomlTestExpectations.load(test);
	}

	private boolean isExpectedInvalidFailure(
		Throwable thrown
	) {
		return thrown instanceof TomlSyntaxException
			|| thrown instanceof TomlRedefineKeyException
			|| thrown instanceof MalformedInputException;
	}
}
