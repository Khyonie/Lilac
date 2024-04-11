package coffee.khyonieheart.lilac.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import coffee.khyonieheart.lilac.Lilac;
import coffee.khyonieheart.lilac.TomlConfiguration;
import coffee.khyonieheart.lilac.TomlParser;

public class TestingApp
{
	private static final Set<String> filter = Set.of("linebreak", "datetime", "table/array", "spec/array-of-tables", "spec/offset", "spec/local", "dotted-4", "unicode.toml", "open-parent-table", "array-subtables");

	public static void main(
		String[] args
	) {
		List<File> validTests = new ArrayList<>(); // Tests that are meant to pass
		List<File> invalidTests = new ArrayList<>(); // Tests that are meant to fail
		List<File> filteredTests = new ArrayList<>();
		collectTests(new File("tests/valid/"), validTests, filteredTests);
		//collectTests(new File("tests/invalid/"), invalidTests, filteredTests);

		Map<Class<? extends Exception>, Integer> failCounts = new HashMap<>();

		TomlParser parser = Lilac.newBuilder()
			.setPreserveComments(true)
			.setUppercaseHex(true)
			.setStoreInlineTypes(true);

		int total = validTests.size() + invalidTests.size();
		int failed = 0;
		int passed = 0;

		long startTime = System.currentTimeMillis();

		for (File test : validTests)
		{
			String document = readFile(test);
			try {
				TomlConfiguration config = parser.getDecoder().decode(document);
				parser.getEncoder().encode(config, parser); // Re-encode to test encoder
				System.out.println("\033[38;5;2m[ PASS ] " + test.getName() + "\033[0m");
				passed++;
			} catch (Exception e) {
				if (!failCounts.containsKey(e.getClass()))
				{
					failCounts.put(e.getClass(), 0);
				}

				System.out.println("\033[48;5;1m[ FAIL ] " + test.getName() + "\033[0m");
				System.out.println(document);
				e.printStackTrace();

				failCounts.put(e.getClass(), failCounts.get(e.getClass()) + 1);
				failed++;
			}
		}

		for (File test : invalidTests)
		{
			String document = readFile(test);
			try {
				TomlConfiguration config = parser.getDecoder().decode(document);
				System.out.println("\033[48;5;1m[ FAIL ] " + test.getName() + "\033[0m");
				System.out.println(document);
				System.out.println("VVV EMITS VVV");
				try {
					System.out.println(parser.getEncoder().encode(config, parser));
				} catch (Exception e) {
					System.out.println("(Encoding failure)");
					e.printStackTrace();
					failed++;
					continue;
				}
				failed++;
			} catch (Exception e) {
				//System.out.println("\033[38;5;2m[ PASS ] " + test.getName() + "(\"" + e.getMessage() + "\")" + "\033[0m");
				passed++;
			}
		}

		System.out.println("Testing complete. Performed " + total + " tests, passed " + passed + "/" + total + ", failed " + failed + "/" + total + " (" + filteredTests.size() + " tests ignored) (" + (((float) passed / total) * 100) + "%) over " + (System.currentTimeMillis() - startTime) + " millis");
		System.out.println("Error breakdown:");
		failCounts.forEach((k, v) -> {
			System.out.println("\t" + k.getSimpleName() + ": " + v);
		});
	}

	private static void collectTests(
		File file,
		List<File> collected,
		List<File> filtered
	) {
		String filepath;
		try {
			filepath = file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (!file.isDirectory())
		{
			if (file.getName().endsWith(".toml"))
			{
				for (String f : filter)
				{
					if (filepath.contains(f))
					{
						filtered.add(file);
						return;
					}
				}
				collected.add(file);
			}
			return;
		}

		for (File subfile : file.listFiles())
		{
			collectTests(subfile, collected, filtered);
		}
	}

	private static String readFile(
		File file
	) {
		StringBuilder builder = new StringBuilder();
		try (Scanner scanner = new Scanner(file))
		{
			while (scanner.hasNextLine())
			{
				builder.append(scanner.nextLine());

				if (scanner.hasNextLine())
				{
					builder.append("\n");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		return builder.toString();
	}
}
