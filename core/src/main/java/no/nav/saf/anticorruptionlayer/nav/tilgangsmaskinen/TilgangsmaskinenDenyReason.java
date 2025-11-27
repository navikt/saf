package no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen;

import lombok.Getter;

import java.util.stream.Stream;

public enum TilgangsmaskinenDenyReason {
	AVVIST_HABILITET("AVVIST_HABILITET"),
	AVVIST_SKJERMING("AVVIST_SKJERMING"),
	AVVIST_GEOGRAFISK("AVVIST_GEOGRAFISK"),
	AVVIST_FORTROLIG_ADRESSE("AVVIST_FORTROLIG_ADRESSE"),
	AVVIST_STRENGT_FORTROLIG_ADRESSE("AVVIST_STRENGT_FORTROLIG_ADRESSE"),
	AVVIST_STRENGT_FORTROLIG_UTLAND("AVVIST_STRENGT_FORTROLIG_UTLAND"),
	AVVIST_PERSON_UTLAND("AVVIST_PERSON_UTLAND"),
	UNKNOWN("UNKNOWN");

	@Getter
	private final String reason;

	TilgangsmaskinenDenyReason(String reason) {
		this.reason = reason;
	}

	public static TilgangsmaskinenDenyReason fromTitle(String title) {
		return Stream.of(values())
				.filter(it -> it.reason.equalsIgnoreCase(title))
				.findFirst()
				.orElse(UNKNOWN);
	}
}

