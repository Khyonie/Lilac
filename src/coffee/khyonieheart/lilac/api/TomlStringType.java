package coffee.khyonieheart.lilac.api;

public enum TomlStringType
{
	BASIC("\""),
	MULTILINE_BASIC("\"\"\""),
	LITERAL("'"),
	MULTILINE_LITERAL("'''")
	;

	private String caps;

	private TomlStringType(
		String caps
	) {
		this.caps = caps;
	}

	public String getStartAndEnd()
	{
		return this.caps;
	}
}
