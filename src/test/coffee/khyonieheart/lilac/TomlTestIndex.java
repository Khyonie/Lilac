package coffee.khyonieheart.lilac;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class TomlTestIndex
{
	private static final String TOML_EXTENSION = ".toml";

	private final List<String> targetFiles;

	private TomlTestIndex(
		List<String> targetFiles
	) {
		this.targetFiles = List.copyOf(targetFiles);
	}

	static TomlTestIndex load(
		TomlVersion version,
		TestSection section
	) throws IOException {
		Path indexFile = Path.of("tests", version == TomlVersion.V1_0_0 ? "files-toml-1.0.0" : "files-toml-1.1.0");
		List<String> targetFiles = Files.readAllLines(indexFile).stream()
			.filter(path -> path.endsWith(TOML_EXTENSION))
			.filter(section::includes)
			.map(path -> path.substring(0, path.length() - TOML_EXTENSION.length()))
			.toList();

		return new TomlTestIndex(targetFiles);
	}

	List<String> targetFiles()
	{
		return this.targetFiles;
	}
}
