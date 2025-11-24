package no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen;

import java.util.List;

public record TilgangsmaskinenBulkResponse(String ansattId, List<Resultat> resultater) {

	public record Resultat(String brukerId, int status, TilgangsmaskinenDenyAnswer detaljer) {

		public boolean isDeny() {
			return this.status != 204;
		}
	}
}

