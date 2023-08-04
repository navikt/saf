package no.nav.saf.query.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.graphql.GraphQLException;

@Slf4j
public record LoggMelding(String journalpostId, String eksternReferanseId) {
	
	public void exceptionLogg(Throwable e) {
		if (e instanceof GraphQLException) {
			log.warn("query journalpost(journalpostId={}, eksternReferanseId={}) feilet. melding={}", journalpostId, eksternReferanseId,
					((GraphQLException) e).getError().getMessage());
		} else if (e instanceof SafFunctionalException) {
			log.error("query journalpost(journalpostId={}, eksternReferanseId={}) teknisk feil. melding={}", journalpostId, eksternReferanseId,
					e.getMessage());
		} else {
			log.error("query journalpost(journalpostId={}, eksternReferanseId={}) ukjent teknisk feil. melding={}",journalpostId, eksternReferanseId,
					e.getMessage());
		}
	}
}
