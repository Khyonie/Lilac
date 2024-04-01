package coffee.khyonieheart.lilac.parser;

import coffee.khyonieheart.lilac.TomlParser;

/**
 * Reference implementation of a TOML parser written with Lilac.
 */
public class LilacParser extends TomlParser
{
	public LilacParser() 
	{
		super(new LilacEncoder(), new LilacDecoder());
	}
}
