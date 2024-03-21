package coffee.khyonieheart.lilac.value;

import coffee.khyonieheart.lilac.api.Commentable;
import coffee.khyonieheart.lilac.api.NumberBase;

public class TomlByte implements Commentable, TomlObject<Byte>
{
	private NumberBase base = NumberBase.DECIMAL;
	private byte value;
	private String comment;

	public TomlByte(
		byte value
	) {
		this.value = value;
	}

	public TomlByte(
		byte value,
		NumberBase base
	) {
		this.value = value;
		this.base = base;
	}

	@Override
	public Byte get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.BYTE;
	}

	public NumberBase getSerializerBase()
	{
		return this.base;
	}

	public TomlByte setSerializerBase(
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
			case BINARY -> "0b" + Integer.toBinaryString(this.value).substring(24);
			case OCTAL -> "0o" + Integer.toOctalString(this.value);
			case DECIMAL -> value + "";
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
