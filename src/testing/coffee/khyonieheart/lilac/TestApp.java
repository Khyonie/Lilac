package coffee.khyonieheart.lilac;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestApp
{
	private static Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
	private static TomlDecoder decoder; 
	private static boolean onlyFails = false;

	/**
	 * Usage:
	 * <command> <(type)> <valid|invalid> <v1.0.0|v1.1.0> [ --fails ]
	 */
	public static void main(String[] args) 
	{
		// Isolate desired tests
		if (args.length < 3)
		{
			System.out.println("Usage: java -jar Lilac.jar <target | all> <valid | invalid> <v1.0.0 | v1.1.0>");
			System.exit(1);
		}

		for (String s : args)
		{
			if (!s.startsWith("--"))
			{
				continue;
			}

			switch (s.substring(2))
			{
				case "fails" -> onlyFails = true;
				default -> throw new IllegalArgumentException("Unknown flag \"" + s + "\"");
			}
		}

		TestType type;
		try {
			type = TestType.valueOf(args[0].toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unrecognized test type \"" + args[0] + "\"");
		}

		TestSection section;
		try {
			section = TestSection.valueOf(args[1].toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unrecognized test section \"" + args[0] + "\"");
		}

		TomlVersion version;
		try {
			version = TomlVersion.valueOf(args[2].toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unknown TOML specification \"" + args[2] + "\"");
		}

		decoder = new LilacDecoder(version);

		// Get target files
		File testFile = version == TomlVersion.V1_0_0 ? new File("tests/files-toml-1.0.0") : new File("tests/files-toml-1.1.0");
		List<String> targetFiles = new ArrayList<>();

		Map<String, TestType> testTypes = new HashMap<>();
		Map<String, TestSection> testSections = new HashMap<>();
		Map<TestType, List<String>> testFamilies = new HashMap<>();
		try (Scanner scanner = new Scanner(testFile))
		{
			while (scanner.hasNext())
			{
				String path = scanner.nextLine();
				if (!path.endsWith(".toml"))
				{
					continue;
				}

				switch (section)
				{
					case VALID -> {
						if (!path.startsWith("valid"))
						{
							continue;
						}
					}
					case INVALID -> {
						if (!path.startsWith("invalid"))
						{
							continue;
						}
					}
					case ALL -> {}
				}

				String test = path.replace(".toml", "");
				String[] fileTestTypeSplit = test.replace("invalid/", "").replace("valid/", "").split("/");
				TestSection fileSection = path.startsWith("valid") ? TestSection.VALID : TestSection.INVALID;
				TestType testType = fileTestTypeSplit.length == 1 ? TestType.BASIC : TestType.fromFolder(fileTestTypeSplit[0]);

				if (type != testType && type != TestType.ALL)
				{
					continue;
				}

				if (!testFamilies.containsKey(testType))
				{
					testFamilies.put(testType, new ArrayList<>());
				}

				targetFiles.add(test);
				testTypes.put(test, testType);
				testSections.put(test, fileSection);
				testFamilies.get(testType).add(test);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Tests:");
		for (String path : targetFiles)
		{
			System.out.println(path);
		}
		System.out.println("Running " + targetFiles.size() + " tests");

		Deque<String> testOutputs = new ArrayDeque<>();
		Set<String> failedTests = new HashSet<>();
		int failed = 0;
		int passed = 0;
		for (String test : targetFiles)
		{
			PassCondition condition = test.startsWith("valid") ? PassCondition.VALID : PassCondition.INVALID;
			System.out.println("> Running test \"" + test + "\" belonging to " + testSections.get(test).name() + " with pass state " + (condition == PassCondition.VALID ? "ACCEPT" : "REJECT"));
			OutputStream stream = new ByteArrayOutputStream();

			PrintStream stdOut = System.out;
			PrintStream stdErr = System.err;
			try (PrintStream output = new PrintStream(stream))
			{
				System.setOut(output);
				System.setErr(output);
				if (test(test, version, condition))
				{
					System.setOut(stdOut);
					System.setErr(stdErr);
					passed++;
					if (!onlyFails)
					{
						testOutputs.push(stream.toString());
					}

					System.out.println(stream.toString());
					continue;
				}

				System.setOut(stdOut);
				System.setErr(stdErr);
				failed++;
				testOutputs.push(stream.toString());
				failedTests.add(test);
			}
		}

		// Readout
		int[][] sectionData = new int[TestType.values().length][3]; // First index is total, second is passes, third is failures
		for (String test : targetFiles)
		{
			sectionData[testTypes.get(test).ordinal()][0]++;
			sectionData[testTypes.get(test).ordinal()][failedTests.contains(test) ? 2 : 1]++;
		}

		for (String output : testOutputs.reversed())
		{
			System.out.println(output);
		}

		System.out.println("######################################## TEST RESULTS ########################################");
		System.out.println("Total tests run: " + targetFiles.size());
		System.out.println("Spec version: " + version.name());
		System.out.println("Test scope: " + type.name() + " -> " + section.name());
		System.out.println("Overall passes: " + passed);
		System.out.println("Overall fails: " + failed);
		System.out.println("Overall pass percentage: " + ((((float) passed) / targetFiles.size()) * 100) + "%");
		System.out.println("Test breakdown:");
		for (TestType targetType : TestType.values())
		{
			if (sectionData[targetType.ordinal()][0] == 0)
			{
				continue;
			}
			int total = sectionData[targetType.ordinal()][0];
			int passes = sectionData[targetType.ordinal()][1];
			String color = passes == total ? "\u001B[1;92m" : "\u001B[1;93m";
			System.out.println("> " + targetType.name() + " -> " + color + passes + "/" + total + " (" + (((float) passes / total) * 100) + "%)\u001B[1;39m");
			for (String test : testFamilies.get(targetType))
			{
				if (failedTests.contains(test))
				{
					System.out.println("   - " + test);
				}
			}
		}

		if (passed == targetFiles.size())
		{
			System.out.println("\u001B[1;92mAll tests passed!\u001B[1;39m");
		}
	}

	private static enum TestType
	{
		BASIC,
		ALL,
		ARRAY,
		BOOLEAN,
		CONTROL,
		COMMENT,
		DATE_TIME,
		ENCODING,
		FLOAT,
		INLINE_TABLE,
		INTEGER,
		KEY,
		LOCAL_DATE,
		LOCAL_DATE_TIME,
		LOCAL_TIME,
		SPEC,
		STRING,
		TABLE
		;

		public static TestType fromFolder(
			String folder
		) {
			return switch (folder) {
				case "" -> TestType.BASIC;
				case "array" -> TestType.ARRAY;
				case "bool" -> TestType.BOOLEAN;
				case "control" -> TestType.CONTROL;
				case "comment" -> TestType.COMMENT;
				case "datetime" -> TestType.DATE_TIME;
				case "encoding" -> TestType.ENCODING;
				case "float" -> TestType.FLOAT;
				case "inline-table" -> TestType.INLINE_TABLE;
				case "integer" -> TestType.INTEGER;
				case "key" -> TestType.KEY;
				case "local-date" -> TestType.LOCAL_DATE;
				case "local-datetime" -> TestType.LOCAL_DATE_TIME;
				case "local-time" -> TestType.LOCAL_TIME;
				case "spec" -> TestType.SPEC;
				case "string" -> TestType.STRING;
				case "table" -> TestType.TABLE;
				default -> throw new UnsupportedOperationException();
			};
		}
	}

	private static enum TestSection
	{
		VALID,
		INVALID,
		ALL
		;
	}

	private static enum PassCondition
	{
		VALID,
		INVALID
		;
	}

	@SuppressWarnings("unchecked")
	private static boolean test(
		String test,
		TomlVersion version,
		PassCondition condition
	) {
		try {
			System.out.append("\u001B[1;33m>>> Running test " + test + ":\u001B[1;39m\n");
			String tomlFile = Files.readString(new File("tests/" + test + ".toml").toPath());
			System.out.append(tomlFile + "\n");
			Map<String, Object> deserialized = decoder.decode(new Document(tomlFile));
			printMap(deserialized, 0);

			if (condition == PassCondition.INVALID)
			{
				System.out.append("\u001B[1;91m==========================================        FAIL        ==========================================\u001B[1;39m");
				return false;
			}

			String jsonFile = Files.readString(new File("tests/" + test + ".json").toPath());
			Map<String, Object> target = gson.fromJson(jsonFile, Map.class);
			System.out.append("> Testing GSON match...\n");

			if (!recursiveEquals(deserialized, target))
			{
				System.out.append("\u001B[1;91m==========================================        FAIL        ==========================================\u001B[1;39m\n");
				System.out.append("Output does not match its corresponding JSON.\n");
				System.out.append(deserialized.toString() + "\n");
				System.out.append("> Target:\n");
				System.out.append(jsonFile + "\n");
				System.out.append("> Deserialized:\n");
				System.out.append(gson.toJson(deserialized) + "\n");
				return false;
			}

			System.out.append("> Testing re-encode match...\n");
			String encoded = new LilacEncoder().encode(deserialized);
			System.out.append(encoded + "\n");
			Map<String, Object> encodeDeserialized = decoder.decode(encoded);

			if (!recursiveEquals(encodeDeserialized, deserialized))
			{
				System.out.append("\u001B[1;91m==========================================        FAIL        ==========================================\u001B[1;39m\n");
				System.out.append("Re-encoded document does not decode to the same document.\n");
				System.out.append("Re-encoded:\n" + encoded + "\n");
				return false;
			}

			System.out.append("\u001B[1;92m==========================================        PASS        ==========================================\u001B[1;39m");
			return true;
		} catch (TomlSyntaxException | TomlRedefineKeyException e) {
			if (condition == PassCondition.INVALID)
			{
				e.printStackTrace();
				System.out.append("\u001B[1;92m==========================================        PASS        ==========================================\u001B[1;39m");
				return true;
			}

			e.printStackTrace();
			System.out.append("\u001B[1;91m==========================================        FAIL        ==========================================\u001B[1;39m");
			return false;
		} catch (MalformedInputException e) {
			System.out.println("(This document cannot be loaded by Java)");
			System.out.append("\u001B[1;92m==========================================        PASS        ==========================================\u001B[1;39m");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.append("\u001B[1;35m========================================== NON-TOML EXCEPTION ==========================================\u001B[1;39m");
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private static void printMap(
		Map<String, Object> map,
		int depth
	) {
		for (String key : map.keySet())
		{
			if (map.get(key) instanceof Map)
			{
				System.out.println("- ".repeat(depth) + key + " ->");
				printMap((Map<String, Object>) map.get(key), depth + 1);
				continue;
			}
			if (map.get(key) instanceof List)
			{
				StringBuilder builder = new StringBuilder("[ ");
				Iterator<Object> iter = ((List<Object>) map.get(key)).iterator();
				while (iter.hasNext())
				{
					builder.append(iter.next());

					if (iter.hasNext())
					{
						builder.append(", ");
					}
				}
				builder.append(" ]");
				System.out.println("- ".repeat(depth) + key + " = " + builder.toString());
				continue;
			}
			System.out.println("- ".repeat(depth) + key + " = " + map.get(key) + " (" + map.get(key).getClass().getSimpleName() + ")");
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean recursiveEquals(
		Map<String, Object> deserialized,
		Map<String, Object> target
	) {
		if (deserialized.size() != target.size())
		{
			System.out.println("Size not equal (" + deserialized.size() + " A/" + target.size() + " B)");
			return false;
		}

		for (String key : deserialized.keySet())
		{
			if (!target.containsKey(key))
			{
				System.out.println("Target does not contain key \"" + key + "\"");
				return false;
			}

			Object deserializedValue = deserialized.get(key);
			Object targetValue = deserialized.get(key);

			if (deserializedValue instanceof List && targetValue instanceof List)
			{
				if (!listEquals((List<Object>) deserializedValue, (List<Object>) targetValue))
				{
					return false;
				}

				continue;
			}

			if (deserializedValue instanceof Map && targetValue instanceof Map)
			{
				if (!recursiveEquals((Map<String, Object>) deserializedValue, (Map<String, Object>) targetValue))
				{
					return false;
				}

				continue;
			}

			if (!deserializedValue.equals(targetValue))
			{
				return false;
			}

			continue;
		}

		return true;
	}

	private static boolean listEquals(
		List<Object> deserialized,
		List<Object> target
	) {
		if (deserialized.size() != target.size())
		{
			return false;
		}

		for (int i = 0; i < deserialized.size(); i++)
		{
			if (!deserialized.get(i).equals(target.get(i)))
			{
				return false;
			}
		}

		return true;
	}
}
