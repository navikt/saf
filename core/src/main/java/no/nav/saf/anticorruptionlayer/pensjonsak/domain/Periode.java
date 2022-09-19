package no.nav.saf.anticorruptionlayer.pensjonsak.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record Periode (
		LocalDate fom,
		LocalDate tom
) {
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ISO_OFFSET_DATE;

	public static Periode of(String fom, String tom) {
		return new Periode(nullsafeParseDate(fom), nullsafeParseDate(tom));
	}

	private static LocalDate nullsafeParseDate(String dateString) {
		return dateString != null ? LocalDate.from(dateFormat.parse(dateString)) : null;
	}
}
