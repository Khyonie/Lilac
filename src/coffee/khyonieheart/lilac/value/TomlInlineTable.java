package coffee.khyonieheart.lilac.value;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import coffee.khyonieheart.lilac.api.Commentable;

public class TomlInlineTable implements Commentable, TomlObject<Map<String, TomlObject<?>>>
{
	private Map<String, TomlObject<?>> backing;
	private String comment = null;
	private int newlines = 0;

	private static Pattern quotedKeyCharacters = Pattern.compile("[^A-Za-z0-9_-]");

	public TomlInlineTable(
		Map<String, TomlObject<?>> data
	) {
		this.backing = data;
	}

	@Override
	public Map<String, TomlObject<?>> get() 
	{
		return this.backing;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.INLINE_TABLE;
	}

	@Override
	public String serialize() 
	{
		StringBuilder builder = new StringBuilder("{ ");

		if (this.backing.isEmpty())
		{
			return builder.append("}").toString();
		}

		Iterator<String> keyIter = backing.keySet().iterator();
		while (keyIter.hasNext())
		{
			String key = keyIter.next();
			TomlObject<?> value = this.backing.get(key);

			if (quotedKeyCharacters.matcher(key).find())
			{
				key = "\"" + key + "\"";
			}

			if (key.length() == 0)
			{
				key = "\"\"";
			}
			
			builder.append(key + " = ");
			builder.append(value.serialize());

			if (keyIter.hasNext())
			{
				builder.append(", ");
			}
		}

		builder.append(" }");

		return builder.toString();
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
	public void setComment(
		String comment
	) {
		this.comment = comment;
	}
}
