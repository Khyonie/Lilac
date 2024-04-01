package coffee.khyonieheart.lilac.value;

import java.util.Objects;

import coffee.khyonieheart.lilac.TomlParser;
import coffee.khyonieheart.lilac.api.Commentable;
import coffee.khyonieheart.lilac.api.NumberBase;

public class TomlInteger implements Commentable, TomlObject<Integer>
{
	private NumberBase base = NumberBase.DECIMAL;
	private int value;
	private String comment;
	private int newlines = 0;

	public TomlInteger(
		int value
	) {
		this.value = value;
	}

	public TomlInteger(
		int value,
		NumberBase base
	) {
		this.value = value;
		this.base = base;
	}

	@Override
	public Integer get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.INTEGER;
	}

	public NumberBase getSerializerBase()
	{
		return this.base;
	}

	public TomlInteger setSerializerBase(
		NumberBase base
	) {
		this.base = base;

		return this;
	}

	public String serialize(
		TomlParser builder
	) {
		Objects.requireNonNull(builder);

		return switch (this.base)
		{
			case BINARY -> "0b" + Integer.toBinaryString(this.value);
			case OCTAL -> "0o" + Integer.toOctalString(this.value);
			case DECIMAL -> value + "";
			case HEXADECIMAL -> "0x" + (builder.getIsUppercaseHex() ? Integer.toHexString(this.value).toUpperCase() : Integer.toHexString(this.value));
		};
	}

	@Override
	public String serialize() 
	{
		return switch (this.base)
		{
			case BINARY -> "0b" + Integer.toBinaryString(this.value);
			case OCTAL -> "0o" + Integer.toOctalString(this.value);
			case DECIMAL -> Integer.toString(this.value);
			case HEXADECIMAL -> "0x" + Integer.toHexString(this.value);
		};
	}

	@Override
	public String getComment() 
	{
		return this.comment;
	}

	@Override
	public void setComment(
		String comment
	) {
		this.comment = comment;
	}

	@Override
	public int getNumberOfTrailingNewlines() 
	{
		return this.newlines;
	}

	@Override
	public void incrementTrailingNewlines() 
	{
		this.newlines++;
	}
}
