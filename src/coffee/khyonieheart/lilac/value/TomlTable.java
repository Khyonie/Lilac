package coffee.khyonieheart.lilac.value;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import coffee.khyonieheart.lilac.api.Commentable;

/**
 * Represents a TOML table.
 *
 * A TOML table can take one of two forms: discrete and indiscrete. Discrete tables can be serialized and will take the form:
 * <code>
 * [key]
 * </code>.
 * Indiscrete tables will instead have their identifier stored as a prefix to a key.
 */
public class TomlTable implements Commentable, TomlObject<Map<String, TomlObject<?>>>
{
	private Map<String, TomlObject<?>> data = new LinkedHashMap<>();
	private boolean isDiscrete = false;
	private String discreteIdentifier = null;
	private List<String> parentTables = null;
	private String comment;
	private int newlines = 0;

	public TomlTable(
		String identifier,
		List<String> parents
	) {
		this.discreteIdentifier = identifier;
		this.parentTables = new ArrayList<>(parents);
		this.isDiscrete = true;
	}

	public TomlTable(
		List<String> parents
	) {
		this.parentTables = new ArrayList<>(parents);
	}

	public TomlTable rebase(
		Map<String, TomlObject<?>> data
	) {
		Objects.requireNonNull(data);

		this.data = data;

		return this;
	}

	@Override
	public Map<String, TomlObject<?>> get() 
	{
		return this.data;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.TABLE;
	}

	public boolean isDiscrete()
	{
		return this.isDiscrete;
	}

	public List<String> getParents()
	{
		return this.parentTables;
	}

	public TomlTable setParents(
		List<String> newParents
	) {
		this.parentTables.addAll(newParents);

		return this;
	}

	public List<String> getCanonicalPath()
	{
		List<String> allParents = new ArrayList<>(this.parentTables);
		allParents.add(this.discreteIdentifier);

		return allParents;
	}

	public String getKey()
	{
		return this.discreteIdentifier;
	}

	@Override
	public String serialize() 
	{
		if (isDiscrete)
		{
			StringBuilder builder = new StringBuilder("[");
			for (String parent : this.parentTables)
			{
				builder.append(parent);
				builder.append('.');
			}

			builder.append(this.discreteIdentifier);
			builder.append(']');

			return builder.toString();
		}

		throw new UnsupportedOperationException("Cannot serialize an indiscrete TOML table");
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
	public TomlTable clone()
	{
		Map<String, TomlObject<?>> clone = new LinkedHashMap<>(this.data.size());

		for (String key : this.data.keySet())
		{
			clone.put(key, this.data.get(key).clone());
		}

		List<String> cloneParents = new ArrayList<>(this.parentTables);

		if (this.discreteIdentifier != null)
		{
			return new TomlTable(this.discreteIdentifier, cloneParents).rebase(clone);
		}

		return new TomlTable(cloneParents).rebase(clone);
	}
}
