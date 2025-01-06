/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2025 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Readers for different RFC 3339 date/time formats.
 */
public class LilacTimes
{
	private static final int YEAR_LENGTH = 4;
	private static final int MONTH_OR_DAY_LENGTH = 2;

	// YYYY-MM-DD
	public static LocalDate tryDate(
		Document document
	) {
		if (document.getCharAtPointer() < '0' || document.getCharAtPointer() > '9')
		{
			return null;
		}

		document.pinPointer();
		char current;

		StringBuilder dateBuilder = new StringBuilder();
		for (int portion = 0; portion < 3; portion++)
		{
			for (int i = 0; i < (portion != 0 ? MONTH_OR_DAY_LENGTH : YEAR_LENGTH); i++)
			{
				current = document.getCharAndIncrement();

				if (current < '0' || current > '9')
				{
					document.rewindToPin();
					return null;
				}

				dateBuilder.append(current);
			}

			// Date separator
			if (portion != 2)
			{
				if (document.getCharAndIncrement() == '-')
				{
					dateBuilder.append('-');
					continue;
				}

				document.rewindToPin();
				return null;
			}
		}

		try {
			document.removePin();
			LocalDate date = LocalDate.parse(dateBuilder.toString());
			return date;
		} catch (DateTimeParseException e) {
			document.rewindToPin();
			throw TomlSyntaxException.of("Invalid local date literal \"" + dateBuilder.toString() + "\"", document.hold());
		}
	}

	// HH:MM:SS.MMMMMMMMM
	public static LocalTime tryTime(
		Document document,
		TomlDecoder decoder
	) {
		if (document.getCharAtPointer() < '0' || document.getCharAtPointer() > '9')
		{
			return null;
		}

		document.pinPointer();
		char current;
		
		StringBuilder timeBuilder = new StringBuilder();
		int millisCount = 0;
		components: for (TimeComponent component : TimeComponent.values())
		{
			for (int i = 0; (component == TimeComponent.MILLISECOND ? true : (i < 2)); i++)
			{
				current = document.getCharAtPointer();

				if (component == TimeComponent.MILLISECOND || (decoder.getVersion() == TomlVersion.V1_1_0 && component == TimeComponent.SECOND))
				{
					if (!document.hasNext() || isOneOfChars(current, '+', '-', 'z', 'Z', '#', ',') || Character.isWhitespace(current))
					{
						if (i == 1 && component == TimeComponent.SECOND)
						{
							throw TomlSyntaxException.of("Incomplete time component", document);
						}
						break components;
					}
				}

				if (current < '0' || current > '9')
				{
					document.rewindToPin();
					return null;
				}

				if (component == TimeComponent.MILLISECOND)
				{
					if (millisCount < 9)
					{
						millisCount++;
						timeBuilder.append(current);
					}

					document.incrementPointer();
					continue;
				}

				document.incrementPointer();
				timeBuilder.append(current);
			}

			int lastTwoDigits = Integer.parseInt(timeBuilder.substring(timeBuilder.length() - 2));

			switch (component)
			{
				case HOUR -> {
					if (lastTwoDigits >= 24)
					{
						document.rewindToPin();
						return null;
					}
				}
				case MINUTE -> {
					if (lastTwoDigits >= 60)
					{
						throw TomlSyntaxException.of("Illegal minute value \"" + lastTwoDigits + "\", expected 0-59", document.hold(3));
					}
				}
				case SECOND -> {
					if (lastTwoDigits >= 60)
					{
						throw TomlSyntaxException.of("Illegal second value \"" + lastTwoDigits + "\", expected 0-59", document.hold(3));
					}
				}
				default -> {}
			}

			// Time separator
			if (component != TimeComponent.MILLISECOND)
			{
				current = document.getCharAtPointer();
				char targetChar = switch (component) {
					case HOUR -> ':';
					case MINUTE -> ':';
					case SECOND -> '.';
					case MILLISECOND -> throw new IllegalStateException();
				};

				if (current != targetChar)
				{
					// v1.1.0 strangeness
					if (decoder.getVersion() == TomlVersion.V1_1_0 && component == TimeComponent.MINUTE && (isOneOfChars(current, '+', '-', 'z', 'Z', '#', ',') || Character.isWhitespace(current)))
					{
						break components;
					}

					if (component == TimeComponent.SECOND && (isOneOfChars(current, '+', '-', 'z', 'Z', '#', ',') || Character.isWhitespace(current))) 
					{
						break components;
					}
					document.rewindToPin();
					return null;
				}

				document.incrementPointer();
				timeBuilder.append(current);
			}
		}

		document.removePin();
		LocalTime time = LocalTime.parse(timeBuilder.toString());
		return time;
	}

	public static ZoneOffset tryOffset(
		Document document
	) {
		if (!isOneOfChars(document.getCharAtPointer(), 'z', 'Z', '+', '-'))
		{
			return null;
		}

		// UTC shorthand
		if (isOneOfChars(document.getCharAtPointer(), 'z', 'Z'))
		{
			document.incrementPointer();
			return ZoneOffset.UTC;
		}

		document.pinPointer();

		// +/-
		StringBuilder prefixBuilder = new StringBuilder();
		prefixBuilder.append(document.getCharAndIncrement());

		// HH:MM
		char current;
		for (int i = 0; i < 5; i++)
		{
			current = document.getCharAndIncrement();
			if (i != 2)
			{
				if (current < '0' || current > '9')
				{
					document.rewindToPin();
					return null;
				}

				prefixBuilder.append(current);
				continue;
			}

			if (current != ':')
			{
				document.rewindToPin();
				return null;
			}

			prefixBuilder.append(':');
		}

		String[] split = prefixBuilder.toString().split(":");
		if (Integer.parseInt(split[0]) >= 24)
		{
			document.rewindToPin();
			throw TomlSyntaxException.of("Invalid hour offset \"" + split[0] + "\", expected 00-23", document);
		}
		if (Integer.parseInt(split[1]) >= 60)
		{
			document.rewindToPin();
			throw TomlSyntaxException.of("Invalid minute offset \"" + split[0] + "\", expected 00-59", document);
		}

		document.removePin();
		ZoneOffset offset = ZoneOffset.of(prefixBuilder.toString());
		return offset;
	}

	private static boolean isOneOfChars(
		char input,
		char... possibleChars
	) {
		for (char c : possibleChars)
		{
			if (c == input)
			{
				return true;
			}
		}

		return false;
	}

	private static enum TimeComponent
	{
		HOUR,
		MINUTE,
		SECOND,
		MILLISECOND
		;
	}
}
