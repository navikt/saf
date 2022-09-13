package no.nav.saf.integration.penrest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record Periode (
		LocalDate fom,
		LocalDate tom
) {
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;

	public static Periode of(String fom, String tom) {
		return new Periode(LocalDate.from(dateFormat.parse(fom)), LocalDate.from(dateFormat.parse(tom)));
	}
}
