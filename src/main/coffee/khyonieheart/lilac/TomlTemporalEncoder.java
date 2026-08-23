/*
 * Lilac, a Java TOML lanugage library
 * Copyright (C) 2026 Hailey-Jane "Khyonie" Garrett
 */ 
package coffee.khyonieheart.lilac;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

final class TomlTemporalEncoder
{
	private TomlTemporalEncoder()
	{}

	static String encode(
		OffsetDateTime odt
	) {
		return encode(odt.toLocalDateTime()) + odt.getOffset().toString();
	}

	static String encode(
		LocalDateTime ldt
	) {
		return encode(ldt.toLocalDate()) + "T" + encode(ldt.toLocalTime());
	}

	static String encode(
		LocalDate date
	) {
		String year = Integer.toString(date.getYear());
		while (year.length() < 4)
		{
			year = "0" + year;
		}

		String month = (date.getMonthValue() >= 10 ? "" : "0") + date.getMonthValue(); 
		String day = (date.getDayOfMonth() >= 10 ? "" : "0") + date.getDayOfMonth();

		return year + "-" + month + "-" + day;
	}

	static String encode(
		LocalTime time
	) {
		String hours = (time.getHour() >= 10 ? "" : "0") + time.getHour();
		String minutes = (time.getMinute() >= 10 ? "" : "0") + time.getMinute();
		String seconds = (time.getSecond() >= 10 ? "" : "0") + time.getSecond();

		return hours + ":" + minutes + ":" + seconds + encodeFraction(time.getNano());
	}

	private static String encodeFraction(
		int nanos
	) {
		if (nanos == 0)
		{
			return "";
		}

		String fraction = Integer.toString(nanos);
		while (fraction.length() < 9)
		{
			fraction = "0" + fraction;
		}

		while (fraction.endsWith("0"))
		{
			fraction = fraction.substring(0, fraction.length() - 1);
		}

		return "." + fraction;
	}
}
