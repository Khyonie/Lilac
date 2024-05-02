package coffee.khyonieheart.lilac;

import coffee.khyonieheart.lilac.parser.LilacParser;

/**
 * Root class for the Lilac TOML library.
 *
 * @author Khyonie
 */
public class Lilac
{
	// Hide no-args constructor
	private Lilac() {}

	private static final int MAJOR_VERSION = 1;
	private static final int MINOR_VERSION = 4;
	private static final int PATCH_REVISION = 1;

	public static int getMajorVersion()
	{
		return MAJOR_VERSION;
	}

	public static int getMinorVersion()
	{
		return MINOR_VERSION;
	}

	public static int getRevision()
	{
		return PATCH_REVISION;
	}

	public static String getVersion()
	{
		return MAJOR_VERSION + "." + MINOR_VERSION + "." + PATCH_REVISION;
	}

	public static TomlParser tomlParser()
	{
		return new LilacParser();
	}

	/**
	 * Creates a new configuration builder.
	 *
	 * @return A new TOML configuration builder, to create a configuration
	 */
	public static TomlConfigurationBuilder newConfiguration()
	{
		return new TomlConfigurationBuilder();
	}
}
