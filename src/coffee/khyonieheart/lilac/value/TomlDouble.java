package coffee.khyonieheart.lilac.value;

import coffee.khyonieheart.lilac.api.Commentable;

public class TomlDouble implements Commentable, TomlObject<Double>
{
	private double value;
	private String comment;
	private int newlines = 0;

	public TomlDouble(
		double value
	) {
		this.value = value;
	}

	@Override
	public Double get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.DOUBLE;
	}

	@Override
	public String serialize() 
	{
		return Double.toString(this.value);
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
	public TomlDouble clone()
	{
		return new TomlDouble(this.value);
	}
}
