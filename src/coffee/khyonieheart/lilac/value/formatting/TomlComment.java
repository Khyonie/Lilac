package coffee.khyonieheart.lilac.value.formatting;

import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlObjectType;

public class TomlComment implements TomlObject<String>
{
	private String comment;

	public TomlComment(
		String comment
	) {
		this.comment = comment;
	}

	@Override
	public String get() 
	{
		return this.comment;
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.COMMENT;
	}

	@Override
	public String serialize() 
	{
		return "#" + this.comment; 
	}
}
