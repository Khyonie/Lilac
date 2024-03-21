package coffee.khyonieheart.lilac;

public class TomlSyntaxException extends Exception
{
	public TomlSyntaxException(
		String error
	) {
		super(error);
	}
}
