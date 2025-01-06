package coffee.khyonieheart.lilac.configuration;

public enum TableTypeContext
{
	/** Table has been explicitly defined in a table header. */
	EXPLICIT,
	/** Table has been implicitly defined by a key. */
	KEY_VALUE_IMPLICIT,
	/** Table has been implicitly defined in a table header. */
	TABLE_IMPLICIT,
	/** Table is an inline table. */
	INLINE 
	;
}
