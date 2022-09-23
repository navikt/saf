package no.nav.saf.anticorruptionlayer.pensjonsak.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record Periode(
		LocalDate fom,
		LocalDate tom
) {
	private static final DateTimeFormatter catchMostDateFormat = DateTimeFormatter
				.ofPattern("[yyyy-MM-dd]['T'[HH:mm:ss][HH:mm][.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]][OOOO][O][z][XXXXX][XXXX]");

	public static Periode of(String fom, String tom) {
		return new Periode(nullsafeParseDate(fom), nullsafeParseDate(tom));
	}

	private static LocalDate nullsafeParseDate(String dateString) {
		if (dateString != null) {
				return LocalDate.from(catchMostDateFormat.parse(dateString));
		}
		return null;
	}
}
