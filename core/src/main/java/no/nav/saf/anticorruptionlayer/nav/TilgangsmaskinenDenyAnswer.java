package no.nav.saf.anticorruptionlayer.nav;

public record TilgangsmaskinenDenyAnswer(
		String type,
		String title,
		int status,
		String instance,
		String brukerIdent,
		String navIdent,
		String begrunnelse,
		String traceId,
		boolean kanOverstyres
) {
}
