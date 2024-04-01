package coffee.khyonieheart.lilac.value;

import java.time.LocalDate;
import java.util.Objects;

import coffee.khyonieheart.lilac.api.Commentable;

public class TomlLocalDate implements Commentable, TomlObject<LocalDate>
{
	private LocalDate value;
	private int newlines;
	private String comment;

	public TomlLocalDate(
		LocalDate value
	) {
		this.value = Objects.requireNonNull(value);
	}

	@Override
	public LocalDate get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.LOCAL_DATE;
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
