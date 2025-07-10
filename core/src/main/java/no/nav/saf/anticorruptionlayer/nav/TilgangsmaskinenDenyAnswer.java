package no.nav.saf.anticorruptionlayer.nav;

public record TilgangsmaskinenDenyAnswer(
		String type,
		Avvisningskode title,
		int status,
		String instance,
		String brukerIdent,
		String navIdent,
		String begrunnelse,
		String traceId,
		boolean kanOverstyres
) {
	public enum Avvisningskode {
		AVVIST_STRENGT_FORTROLIG_ADRESSE,
		AVVIST_STRENGT_FORTROLIG_UTLAND,
		AVVIST_AVDØD,
		AVVIST_PERSON_UTLAND,
		AVVIST_SKJERMING,
		AVVIST_FORTROLIG_ADRESSE,
		AVVIST_UKJENT_BOSTED,
		AVVIST_GEOGRAFISK,
		AVVIST_HABILITET
	}
}
