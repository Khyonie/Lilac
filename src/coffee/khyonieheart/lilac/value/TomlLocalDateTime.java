package coffee.khyonieheart.lilac.value;

import java.time.LocalDateTime;
import java.util.Objects;

import coffee.khyonieheart.lilac.api.Commentable;

public class TomlLocalDateTime implements Commentable, TomlObject<LocalDateTime>
{
	private LocalDateTime value;
	private int newlines;
	private String comment;

	public TomlLocalDateTime(
		LocalDateTime value
	) {
		this.value = Objects.requireNonNull(value);
	}

	@Override
	public LocalDateTime get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.LOCAL_DATE_TIME;
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

	@Override
	public TomlLocalDateTime clone()
	{
		return new TomlLocalDateTime(this.value.plusDays(0)); // See TomlLocalDate
	}
}
