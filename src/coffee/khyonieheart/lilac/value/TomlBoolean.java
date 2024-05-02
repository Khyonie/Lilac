package coffee.khyonieheart.lilac.value;

import coffee.khyonieheart.lilac.api.Commentable;

public class TomlBoolean implements Commentable, TomlObject<Boolean>
{
	private boolean value;
	private String comment;
	private int newlines = 0;

	public TomlBoolean(
		boolean value
	) {
		this.value = value;
	}

	@Override
	public Boolean get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.BOOLEAN;
	}

	@Override
	public String serialize() 
	{
		return Boolean.toString(value);
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
	public TomlBoolean clone()
	{
		return new TomlBoolean(this.value);
	}
}
