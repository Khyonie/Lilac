package coffee.khyonieheart.lilac.value;

import coffee.khyonieheart.lilac.api.Commentable;

public class TomlFloat implements Commentable, TomlObject<Float>
{
	private float value;
	private String comment;
	private int newlines = 0;

	public TomlFloat(
		float value
	) {
		this.value = value;
	}

	@Override
	public Float get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.FLOAT;
	}

	@Override
	public String serialize() 
	{
		if (this.value == Float.POSITIVE_INFINITY)
		{
			return "inf";
		}

		if (this.value == Float.NEGATIVE_INFINITY)
		{
			return "-inf";
		}

		if (this.value == Float.NaN)
		{
			return "nan";
		}

		return Float.toString(this.value);
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

	@Override
	public TomlFloat clone()
	{
		return new TomlFloat(this.value);
	}
}
