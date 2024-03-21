package coffee.khyonieheart.lilac.value;

import coffee.khyonieheart.lilac.api.Commentable;
import coffee.khyonieheart.lilac.api.NumberBase;

public class TomlLong implements Commentable, TomlObject<Long>
{
	private NumberBase base = NumberBase.DECIMAL;
	private long value;
	private String comment;

	public TomlLong(
		long value
	) {
		this.value = value;
	}

	public TomlLong(
		long value,
		NumberBase base
	) {
		this.value = value;
		this.base = base;
	}

	@Override
	public Long get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.LONG;
	}

	public NumberBase getSerializerBase()
	{
		return this.base;
	}

	public TomlLong setSerializerBase(
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
			case BINARY -> "0b" + Long.toBinaryString(this.value);
			case OCTAL -> "0o" + Long.toOctalString(this.value);
			case DECIMAL -> value + "";
			case HEXADECIMAL -> "0x" + Long.toHexString(this.value);
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
