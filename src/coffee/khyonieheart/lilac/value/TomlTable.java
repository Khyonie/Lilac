package coffee.khyonieheart.lilac.value;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
}
