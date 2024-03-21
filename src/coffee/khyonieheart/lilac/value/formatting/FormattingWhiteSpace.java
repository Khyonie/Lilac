package coffee.khyonieheart.lilac.value.formatting;

import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlObjectType;

public class FormattingWhiteSpace implements TomlObject<String>
{

	@Override
	public String get() {
		return "\n";
	}

	@Override
	public TomlObjectType getType() 
	{
		return TomlObjectType.EMPTY_LINE;
	}

	@Override
	public String serialize() 
	{
		return "\n";
	}
}
