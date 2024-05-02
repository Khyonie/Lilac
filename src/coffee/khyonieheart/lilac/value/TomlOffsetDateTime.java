package coffee.khyonieheart.lilac.value;

import java.time.OffsetDateTime;
import java.util.Objects;

import coffee.khyonieheart.lilac.api.Commentable;

public class TomlOffsetDateTime implements Commentable, TomlObject<OffsetDateTime>
{
	private OffsetDateTime value;
	private int newlines;
	private String comment;

	public TomlOffsetDateTime(
		OffsetDateTime value
	) {
		this.value = Objects.requireNonNull(value);
	}

	@Override
	public OffsetDateTime get() 
	{
		return this.value;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.OFFSET_DATE_TIME;
	}

	@Override
	public String serialize() 
	{
		// TODO This may not be TOML compliant if seconds and/or millis are 0
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
	public TomlOffsetDateTime clone()
	{
		return new TomlOffsetDateTime(this.value.plusNanos(0)); // See TomlLocalDate
	}
}
