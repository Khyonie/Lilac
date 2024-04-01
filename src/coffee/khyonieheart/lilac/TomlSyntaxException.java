package coffee.khyonieheart.lilac;

public class TomlSyntaxException extends Exception
{
	private int line;
	private int position;
	private int length;
	private String document;

	public TomlSyntaxException(
		String message,
		int line,
		int position,
		int length,
		String document
	) {
		super(message);
		this.line = line;
		this.position = position;
		this.length = length;
		this.document = document;
	}

	@Override
	public void printStackTrace()
	{
		String lineString = document.split("\n")[line] + "↩";

		System.out.println("TOML syntax error: " + this.getMessage() + " at line " + (line + 1) + ", position " + (this.position + 1));
		System.out.println(lineString);
		System.out.println(" ".repeat(position) + "^".repeat(this.length) + " (Here)");

		super.printStackTrace();
	}
}
