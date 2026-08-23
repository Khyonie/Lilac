package coffee.khyonieheart.lilac;

enum TestSection
{
	VALID("valid/"),
	INVALID("invalid/")
	;

	private final String pathPrefix;

	private TestSection(
		String pathPrefix
	) {
		this.pathPrefix = pathPrefix;
	}

	boolean includes(
		String path
	) {
		return path.startsWith(pathPrefix);
	}
}
