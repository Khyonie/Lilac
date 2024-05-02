package coffee.khyonieheart.lilac;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public interface TomlDecoder
{
	public TomlConfiguration decode(
		String document
	)
		throws TomlSyntaxException;

	public default TomlConfiguration decode(
		File file
	)
		throws FileNotFoundException,
			TomlSyntaxException
	{
		StringBuilder builder = new StringBuilder();

		try (Scanner scanner = new Scanner(file))
		{
			while (scanner.hasNext())
			{
				builder.append(scanner.nextLine());

				if (scanner.hasNext())
				{
					builder.append('\n');
				}
			}
		}

		return this.decode(builder.toString());
	}
}
