package coffee.khyonieheart.lilac.value;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TomlTableArray implements TomlObject<List<Map<String, TomlObject<?>>>>
{
	private List<String> keys;
	private List<Map<String, TomlObject<?>>> data = new ArrayList<>();

	private Map<String, TomlObject<?>> targetMap;

	public TomlTableArray(
		List<String> keys
	) {
		this.keys = Objects.requireNonNull(keys);
		this.targetMap = new LinkedHashMap<>();
	}

	public void addToTarget(
		List<String> keys, 
		TomlObject<?> value
	) {
		String key = keys.get(keys.size() - 1);
		List<String> parents = keys.subList(0, keys.size() - 1);

		List<String> interimParents = new ArrayList<>();

		Map<String, TomlObject<?>> data = targetMap;
		for (String parent : parents)
		{
			if (!data.containsKey(parent))
			{
				data.put(parent, new TomlTable(interimParents));
			}

			if (!(data.get(parent) instanceof TomlTable))
			{
				throw new UnsupportedOperationException("Cannot redefine existing key " + parent + " with type " + data.get(parent).getType().name() + " as a table");
			}

			data = ((TomlTable) data.get(parent)).get();
		}

		data.put(key, value);
	}

	public boolean equalsTable(
		TomlTableArray tableArray
	) {
		if (tableArray == null)
		{
			return false;
		}

		return this.keysMatch(tableArray.keys);
	}

	private boolean keysMatch(
		List<String> keys
	) {
		if (this.keys.size() != keys.size())
		{
			return false;
		}

		for (int i = 0; i < this.keys.size(); i++)
		{
			if (!this.keys.get(i).equals(keys.get(i)))
			{
				return false;
			}
		}

		return true;
	}

	public void startNextTable()
	{
		targetMap = new LinkedHashMap<>();
		data.add(targetMap);
	}

	@Override
	public List<Map<String, TomlObject<?>>> get() 
	{
		return this.data;
	}

	public Map<String, TomlObject<?>> currentTable()
	{
		return this.targetMap;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.TABLE_ARRAY;
	}

	@Override
	public String serialize() 
	{
		StringBuilder builder = new StringBuilder();

		// TODO This

		return builder.toString();
	}

	@Override
	public int getNumberOfTrailingNewlines() 
	{
		throw new UnsupportedOperationException("Table arrays do not themselves contain newlines");
	}

	@Override
	public void incrementTrailingNewlines() 
	{
		throw new UnsupportedOperationException("Table arrays do not themselves contain newlines");
	}
}
