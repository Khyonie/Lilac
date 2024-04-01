package coffee.khyonieheart.lilac.value;

import java.time.LocalTime;
import java.util.Objects;

import coffee.khyonieheart.lilac.api.Commentable;

public class TomlLocalTime implements Commentable, TomlObject<LocalTime>
{
	private LocalTime value;
	private int newlines;
	private String comment;

	public TomlLocalTime(
		LocalTime value
	) {
		this.value = Objects.requireNonNull(value);
	}

	@Override
	public LocalTime get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.LOCAL_TIME;
	}

	@Override
	public String serialize() 
	{
		return value.toString();
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
	public String getComment() 
	{
		return this.comment;
	}

	@Override
	public void setComment(String comment) 
	{
		this.comment = comment;
	}
}
