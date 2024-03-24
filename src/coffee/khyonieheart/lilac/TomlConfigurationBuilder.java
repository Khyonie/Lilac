package coffee.khyonieheart.lilac;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import coffee.khyonieheart.lilac.api.Commentable;
import coffee.khyonieheart.lilac.api.NumberBase;
import coffee.khyonieheart.lilac.value.TomlArray;
import coffee.khyonieheart.lilac.value.TomlBoolean;
import coffee.khyonieheart.lilac.value.TomlByte;
import coffee.khyonieheart.lilac.value.TomlDouble;
import coffee.khyonieheart.lilac.value.TomlFloat;
import coffee.khyonieheart.lilac.value.TomlInlineTable;
import coffee.khyonieheart.lilac.value.TomlInteger;
import coffee.khyonieheart.lilac.value.TomlLong;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlShort;
import coffee.khyonieheart.lilac.value.TomlString;

/**
 * A utility that helps with creating a TOML configuration. Provides utilites for setting up defaults and sanity checking.
 */
public class TomlConfigurationBuilder
{
	private TomlObject<?> formattingTarget = null;
	private TomlConfiguration configuration;

	TomlConfigurationBuilder()
	{
		this.configuration = new TomlConfiguration(new LinkedHashMap<>());
	}

	// Setters
	//-------------------------------------------------------------------------------- 

	public TomlConfigurationBuilder add(
		String key,
		TomlObject<?> object
	) {
		return add(object, TomlKeys.extractKeys(key));
	}

	public TomlConfigurationBuilder add(
		TomlObject<?> object,
		String... keys
	) {
		Objects.requireNonNull(keys);
		this.formattingTarget = Objects.requireNonNull(object);

		if (keys.length == 0)
		{
			throw new IllegalArgumentException("At least one key must be given");
		}

		configuration.set(object, keys);

		return this;
	}

	public TomlConfigurationBuilder addString(
		String key,
		String value
	) {
		return this.add(key, new TomlString(value));
	}

	public TomlConfigurationBuilder addBoolean(
		String key,
		boolean value
	) {
		return this.add(key, new TomlBoolean(value));
	}

	public TomlConfigurationBuilder addMap(
		String key,
		Map<String, TomlObject<?>> map
	) {
		return this.add(key, new TomlInlineTable(map));
	}

	public TomlConfigurationBuilder addList(
		String key,
		List<TomlObject<?>> list
	) {
		return this.add(key, new TomlArray(list));
	}

	// Integer types
	//-------------------------------------------------------------------------------- 

	public TomlConfigurationBuilder addByte(
		String key,
		byte value
	) {
		return this.add(key, new TomlByte(value));
	}

	public TomlConfigurationBuilder addByte(
		String key,
		byte value,
		NumberBase base
	) {
		return this.add(key, new TomlByte(value, base));
	}

	public TomlConfigurationBuilder addShort(
		String key,
		short value
	) {
		return this.add(key, new TomlShort(value));
	}

	public TomlConfigurationBuilder addShort(
		String key,
		short value,
		NumberBase base
	) {
		return this.add(key, new TomlShort(value, base));
	}

	public TomlConfigurationBuilder addInteger(
		String key,
		int value
	) {
		return this.add(key, new TomlInteger(value));
	}

	public TomlConfigurationBuilder addByte(
		String key,
		int value,
		NumberBase base
	) {
		return this.add(key, new TomlInteger(value, base));
	}

	public TomlConfigurationBuilder addLong(
		String key,
		long value
	) {
		return this.add(key, new TomlLong(value));
	}

	public TomlConfigurationBuilder addLong(
		String key,
		long value,
		NumberBase base
	) {
		return this.add(key, new TomlLong(value, base));
	}

	// Decimal types
	//-------------------------------------------------------------------------------- 
	public TomlConfigurationBuilder addFloat(
		String key,
		float value
	) {
		return this.add(key, new TomlFloat(value));
	}

	public TomlConfigurationBuilder addDouble(
		String key,
		double value
	) {
		return this.add(key, new TomlDouble(value));
	}

	// Formatting
	//-------------------------------------------------------------------------------- 
	public void setInlineComment(
		String comment
	) {
		Objects.requireNonNull(comment);

		if (this.formattingTarget == null)
		{
			throw new IllegalStateException("A commentable value must be added before comments or whitespace can be added");
		}

		if (this.formattingTarget instanceof Commentable c)
		{
			c.setComment(comment);
			return;
		}

		throw new IllegalArgumentException("Cannot insert comment on non-commentable type " + this.formattingTarget.getType().name());
	}

	public void addWhitespace() 
	{
		if (this.formattingTarget == null)
		{
			throw new IllegalStateException("A commentable value must be added before comments or whitespace can be added");
		}

		this.formattingTarget.incrementTrailingNewlines();
	}

	public void addWhitespace(
		int lines
	) {
		if (lines < 1)
		{
			throw new IllegalArgumentException("Must add at least one whitespace line");
		}

		for (int i = 0; i < lines; i++)
		{
			addWhitespace();
		}
	}
}
