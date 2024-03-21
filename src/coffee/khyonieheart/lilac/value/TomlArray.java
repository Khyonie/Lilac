package coffee.khyonieheart.lilac.value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import coffee.khyonieheart.lilac.api.Commentable;

/**
 * Represents a TOML array. 
 *
 * Arrays in TOML are different than in Java, whereas Java arrays are constrained to one type, TOML arrays behave much more similarly to <code>List</code>s of <code>Object</code>s.
 * As such, they are implemented using a List rather than directly using an array, and are mutable and growable. This implementation guarantees preservation of iteration order.
 */
public class TomlArray implements Commentable, TomlObject<List<TomlObject<?>>>
{
	private List<TomlObject<?>> data = new ArrayList<>();
	private String comment;
	private int newlines = 0;

	public TomlArray(
		TomlObject<?>... objects
	) {
		for (TomlObject<?> o : objects)
		{
			this.data.add(o);
		}
	}

	public TomlArray(
		Collection<TomlObject<?>> data
	) {
		this.data.addAll(data);
	}

	@Override
	public List<TomlObject<?>> get() 
	{
		return data;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.ARRAY;
	}

	@Override
	public String serialize() 
	{
		StringBuilder builder = new StringBuilder("[ ");

		if (!data.isEmpty())
		{
			int index = 0;
			builder.append(this.data.get(index).serialize());

			for (; index < this.data.size(); index++)
			{
				builder.append(", ");
				builder.append(this.data.get(index).serialize());
			}
		}

		builder.append(" ]");

		return builder.toString();
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
}
