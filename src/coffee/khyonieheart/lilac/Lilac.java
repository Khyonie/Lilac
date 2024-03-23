package coffee.khyonieheart.lilac;

import coffee.khyonieheart.lilac.api.LilacTomlBuilder;

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
	private static final int MINOR_VERSION = 2;
	private static final int PATCH_REVISION = 2;

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

	/**
	 * Creates a new TOML builder.
	 *
	 * @return A new TOML builder to configure
	 */
	public static TomlBuilder newBuilder()
	{
		return new LilacTomlBuilder();
	}
}
