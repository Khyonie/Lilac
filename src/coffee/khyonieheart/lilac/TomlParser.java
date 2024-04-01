package coffee.khyonieheart.lilac;

import java.util.Objects;

/**
 * Combination of an encoder and decoder. Provides settings to configure encoding and decoding.
 *
 * @author Khyonie
 * @since 1.3.0
 */
public class TomlParser
{
	private final TomlEncoder encoder;
	private final TomlDecoder decoder;

	private boolean inlineTypes = false;
	private boolean preserveComments = true;
	private boolean uppercaseHex = true;
	private boolean separateArrayIntoLines = false;

	public TomlParser(
		TomlEncoder encoder,
		TomlDecoder decoder
	) {
		this.encoder = Objects.requireNonNull(encoder);
		this.decoder = Objects.requireNonNull(decoder);
	}

	public TomlEncoder getEncoder() 
	{
		return encoder;
	}

	public TomlDecoder getDecoder() 
	{
		return decoder;
	}

	public boolean getStoreInlineTypes() 
	{
		return inlineTypes;
	}

	public TomlParser setStoreInlineTypes(
		boolean inlineTypes
	) {
		this.inlineTypes = inlineTypes;

		return this;
	}

	public boolean getPreserveComments() 
	{
		return preserveComments;
	}

	public TomlParser setPreserveComments(
		boolean preserveComments
	) {
		this.preserveComments = preserveComments;

		return this;
	}

	public boolean getIsUppercaseHex() 
	{
		return uppercaseHex;
	}

	public TomlParser setUppercaseHex(
		boolean uppercaseHex
	) {
		this.uppercaseHex = uppercaseHex;

		return this;
	}

	public boolean getSeparateArrayIntoLines() 
	{
		return separateArrayIntoLines;
	}

	public void setSeparateArrayIntoLines(
		boolean separateArrayIntoLines
	) {
		this.separateArrayIntoLines = separateArrayIntoLines;
	}
}
