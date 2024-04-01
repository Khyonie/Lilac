package coffee.khyonieheart.lilac.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import coffee.khyonieheart.lilac.TomlConfiguration;
import coffee.khyonieheart.lilac.TomlDecoder;
import coffee.khyonieheart.lilac.TomlSyntaxException;
import coffee.khyonieheart.lilac.parser.analyzer.ParserStep;
import coffee.khyonieheart.lilac.parser.productions.ProductionComment;
import coffee.khyonieheart.lilac.parser.productions.ProductionDiscreteTable;
import coffee.khyonieheart.lilac.parser.productions.ProductionKeyValuePair;
import coffee.khyonieheart.lilac.parser.productions.ProductionTableArray;
import coffee.khyonieheart.lilac.value.TomlInlineTable;
import coffee.khyonieheart.lilac.value.TomlObject;
import coffee.khyonieheart.lilac.value.TomlTable;
import coffee.khyonieheart.lilac.value.TomlTableArray;
import coffee.khyonieheart.lilac.value.formatting.TomlComment;

public class LilacDecoder implements TomlDecoder
{
	private static final String TOML_COMMENT_KEY = "Lilac#TomlComment";

	private int[] pointer = new int[] { 0 };
	private String document;
	private Map<String, Pattern> cachedRegexPatterns = new HashMap<>();
	private Map<String, TomlObject<?>> tomlData;

	private List<ParserStep> steps = new ArrayList<>();

	private int line = 1;
	private int linePointer = 0;

	private TomlTableArray currentTableArray = null;
	private Map<String, TomlObject<?>> currentTable = null;

	@Override
	public TomlConfiguration decode(
		String document
	)
		throws TomlSyntaxException
	{
		this.document = Objects.requireNonNull(document);
		if (document.isEmpty())
		{
			return new TomlConfiguration(new LinkedHashMap<>());
		}

		// Reset state

		this.pointer = new int[] { 0 };
		this.line = 0;
		this.linePointer = 0;
		this.steps.clear();
		this.currentTableArray = null;

		this.tomlData = new LinkedHashMap<>();
		this.currentTable = tomlData;

		while (pointer[0] < this.document.length())
		{
			addStep("----- New Construct -----");
			while (consumeCharacters(' ', '\t'));
			while (consumeCharacters('\n'))
			{
				nextLine();
				while (consumeCharacters(' ', '\n'));
			}

			// Array of tables
			if (ProductionTableArray.parse(this))
			{
				while (this.consumeCharacters(' ', '\t'));
				while (this.consumeCharacters('\n'))
				{
					this.nextLine();
					while (this.consumeCharacters(' ', '\t'));
				}

				continue;
			}

			// Discrete table
			Optional<TomlTable> tableOption = ProductionDiscreteTable.parse(this);
			if (tableOption.isPresent())
			{
				this.currentTable = null;
				addKeyValuePair(tableOption.get().getCanonicalPath(), tableOption.get());
				this.currentTable = tableOption.get().get();
				continue;
			}

			// Key/value pair
			if (ProductionKeyValuePair.parse(this))
			{
				continue;
			}

			// Comment
			Optional<String> commentOption = ProductionComment.parse(this);
			if (commentOption.isPresent())
			{
				TomlComment comment = new TomlComment(commentOption.get());

				while (this.consumeCharacters('\n'))
				{
					this.nextLine();
					comment.incrementTrailingNewlines();
					while (this.consumeCharacters(' ', '\t'));
				}

				int presentIndex = 0;
				if (this.currentTableArray != null)
				{
					while (this.currentTableArray.currentTable().containsKey(TOML_COMMENT_KEY + presentIndex))
					{
						presentIndex++;
					}
					this.currentTableArray.addToTarget(List.of(TOML_COMMENT_KEY + presentIndex), comment);

					continue;
				}

				while (currentTable.containsKey(TOML_COMMENT_KEY + presentIndex))
				{
					presentIndex++;
				}

				currentTable.put(TOML_COMMENT_KEY + presentIndex, comment);
				continue;
			}

			break;
		}

		if (this.pointer[0] < document.length())
		{
			throw new TomlSyntaxException("Failed to consume document", line, this.linePointer, 1, document);
		}

		return new TomlConfiguration(this.tomlData);
	}

	public Optional<String> parseRegex(
		String regex
	) {
		steps.add(new ParserStep("Matching regex " + regex, pointer[0], pointer[0]));
		if (!this.cachedRegexPatterns.containsKey(regex))
		{
			this.cachedRegexPatterns.put(regex, Pattern.compile(regex));
		}

		Matcher matcher = this.cachedRegexPatterns.get(regex).matcher(this.document);

		if (!matcher.find(pointer[0]))
		{
			steps.add(new ParserStep("Pattern not found", pointer[0], pointer[0]));
			return Optional.empty();
		}

		if (matcher.start() != this.pointer[0])
		{
			steps.add(new ParserStep("Pattern found at different position", matcher.start(), matcher.end()));
			return Optional.empty();
		}

		steps.add(new ParserStep("Match found: " + matcher.group(), matcher.start(), matcher.end()));
		incrementPointer(matcher.group().length());
		return Optional.of(matcher.group(1));
	}

	public boolean parseLiteral(
		String literal
	) {
		steps.add(new ParserStep("Matching literal " + literal, pointer[0], pointer[0] + literal.length()));
		int endPointer = Math.min(this.document.length(), this.pointer[0] + literal.length());

		String substring = this.document.substring(this.pointer[0], endPointer);
		if (!substring.equals(literal))
		{
			steps.add(new ParserStep("Substring " + substring + " does not match " + literal, pointer[0], pointer[0]));
			return false;
		}

		steps.add(new ParserStep("Literal found", pointer[0], endPointer));
		incrementPointer(literal.length());
		return true;
	}

	public boolean consumeCharacters(char... chars)
	{
		steps.add(new ParserStep("Attempting to consume whitespace", pointer[0], pointer[0]));

		if (pointer[0] >= document.length())
		{
			steps.add(new ParserStep("End of document reached", pointer[0], pointer[0]));
			return false;
		}

		for (char c : chars)
		{
			steps.add(new ParserStep("Matching character '" + (c == '\n' ? "\\n" : c) + "'", pointer[0], pointer[0]));
			if (this.document.charAt(this.pointer[0]) == c)
			{
				steps.add(new ParserStep("Character matched", pointer[0], pointer[0]));
				incrementPointer(1);
				return true;
			}
		}

		steps.add(new ParserStep("No match found", pointer[0], pointer[0]));
		return false;
	}

	public void toNextSymbol()
	{
		while (consumeCharacters(' ', '\t'));
		while (consumeCharacters('\n'))
		{
			nextLine();
			while (consumeCharacters(' ', '\t'));
		}
	}

	public void addKeyValuePair(
		List<String> keys,
		TomlObject<?> value
	)
		throws TomlSyntaxException
	{
		if (this.currentTableArray != null)
		{
			this.currentTableArray.addToTarget(keys, value);
			return;
		}

		String key = keys.get(0); // Root key
		List<String> parents = new ArrayList<>();
		Map<String, TomlObject<?>> targetMap = (this.currentTable != null ? this.currentTable : this.tomlData);

		Iterator<String> keyIter = keys.iterator();
		while (keyIter.hasNext())
		{
			key = keyIter.next();

			if (!keyIter.hasNext())
			{
				break;
			}

			if (!targetMap.containsKey(key))
			{
				targetMap.put(key, new TomlTable(new ArrayList<>(parents)));
			}

			targetMap = switch (targetMap.get(key).getType())
			{
				case TABLE -> ((TomlTable) targetMap.get(key)).get();
				case INLINE_TABLE -> throw new TomlSyntaxException("Cannot modify inline table after creation", this.line, this.linePointer, key.length(), document);
				case TABLE_ARRAY -> throw new TomlSyntaxException("Cannot modify table array after creation", this.line, this.linePointer, key.length(), document);
				default -> {
					StringBuilder keyBuilder = new StringBuilder();
					for (String s : parents)
					{
						keyBuilder.append(s);
						keyBuilder.append('.');
					}
					keyBuilder.append(key);

					throw new TomlSyntaxException("Cannot redefine existing key " + keyBuilder.toString() + " with type " + targetMap.get(key).getType().name() + " as a table", this.line, this.linePointer, key.length(), this.document);
				}
			};

			parents.add(key);
		}

		if (targetMap.containsKey(key))
		{
			throw new TomlSyntaxException("Cannot overwrite existing key " + key + " with type " + targetMap.get(key).getType(), line, this.linePointer, key.length(), document);
		}

		targetMap.put(key, value);
	}

	public String getCurrentDocument()
	{
		return this.document;
	}

	public void incrementPointer(
		int delta
	) {
		addStep("Incrementing pointer by " + delta + ": g" + this.pointer[0] + " → " + (this.pointer[0] + delta) + " | l" + this.linePointer + " → " + (this.linePointer + delta));
		this.pointer[0] += delta;
		this.linePointer += delta;
	}

	public int getPointer()
	{
		return this.pointer[0];
	}

	public int getLine()
	{
		return this.line;
	}

	public int getLinePointer()
	{
		return this.linePointer;
	}

	public char charAtPointer()
	{
		addStep("Getting character at pointer: '" + this.document.charAt(this.pointer[0]) + "'");
		return this.document.charAt(this.pointer[0]);
	}

	public void nextLine()
	{
		addStep("Next line");
		this.line++;
		this.linePointer = 0;
	}

	public Map<String, TomlObject<?>> getTomlData()
	{
		return this.tomlData;
	}

	public void setCurrentTableArray(
		TomlTableArray tableArray
	) {
		this.currentTableArray = Objects.requireNonNull(tableArray);
	}

	public TomlTableArray getCurrentTableArray()
	{
		return this.currentTableArray;
	}

	public void clearCurrentTableArray()
	{
		this.currentTableArray = null;
	}
	
	public void addStep(
		ParserStep step
	) {
		steps.add(step);
	}

	public void addStep(
		String step
	) {
		steps.add(new ParserStep(step, pointer[0], pointer[0]));
	}

	public List<ParserStep> getSteps()
	{
		return this.steps;
	}
}
