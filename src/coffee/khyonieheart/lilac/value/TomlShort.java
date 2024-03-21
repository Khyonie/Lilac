package coffee.khyonieheart.lilac.value;

import coffee.khyonieheart.lilac.api.Commentable;
import coffee.khyonieheart.lilac.api.NumberBase;

public class TomlShort implements Commentable, TomlObject<Short>
{
	private NumberBase base = NumberBase.DECIMAL;
	private short value;
	private String comment;

	public TomlShort(
		short value
	) {
		this.value = value;
	}

	public TomlShort(
		short value,
		NumberBase base
	) {
		this.value = value;
		this.base = base;
	}

	@Override
	public Short get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.SHORT;
	}

	public NumberBase getSerializerBase()
	{
		return this.base;
	}

	public TomlShort setSerializerBase(
		NumberBase base
	) {
		this.base = base;

		return this;
	}

	@Override
	public String serialize() 
	{
		return switch (this.base)
		{
			case BINARY -> "0b" + Integer.toBinaryString(this.value).substring(16);
			case OCTAL -> "0o" + Integer.toOctalString(this.value);
			case DECIMAL -> value + ":s";
			case HEXADECIMAL -> "0x" + Integer.toHexString(this.value).substring(4);
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
}
