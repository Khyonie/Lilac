package coffee.khyonieheart.lilac.api;

public enum StringType
{
	BASIC("\""),
	MULTILINE_BASIC("\"\"\""),
	LITERAL("'"),
	MULTILINE_LITERAL("'''")
	;

	private String caps;

	private StringType(
		String caps
	) {
		this.caps = caps;
	}

	public String getStartAndEnd()
	{
		return this.caps;
	}
}
