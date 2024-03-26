package coffee.khyonieheart.lilac;

public class TomlSyntaxException extends Exception
{
	private int position;
	private String document;

	public TomlSyntaxException(
		String error,
		int position,
		String document
	) {
		super(error);
		this.position = position;
		this.document = document;
	}

	@Override
	public void printStackTrace()
	{
		// Figure out the current line
		System.out.println("TOML error: " + this.getMessage());
		int lineStart = position;
		while (lineStart >= 0 && document.charAt(lineStart) != '\n')
		{
			lineStart--;
		}
		lineStart++;
		int lineEnd = position;
		while (lineEnd < (document.length() - 1) && document.charAt(lineEnd) != '\n')
		{
			lineEnd++;
		}

		String line = document.subSequence(lineStart, lineEnd).toString();
		int errorPosition = position - lineStart + 1; // We offset by 1 to account for the pointer not incrementing on a syntax error
		String errorArrow = " ".repeat(errorPosition) + "^ (Error here at position " + position + ")";

		System.out.println(line);
		System.out.println(errorArrow);

		super.printStackTrace();
	}
}
