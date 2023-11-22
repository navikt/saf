package no.nav.saf.query.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.graphql.GraphQLException;

@Slf4j
public record LoggMelding(String journalpostId, String eksternReferanseId) {
	
	public void exceptionLogg(Throwable e) {
		if (e instanceof GraphQLException graphQLException) {
			log.warn("query journalpost(journalpostId={}, eksternReferanseId={}) feilet. melding={}",
					journalpostId, eksternReferanseId, graphQLException.getError().getMessage());
		} else if (e instanceof SafFunctionalException safException) {
			log.error("query journalpost(journalpostId={}, eksternReferanseId={}) funksjonell feil. melding={}",
					journalpostId, eksternReferanseId, safException.getMessage());
		} else {
			log.error("query journalpost(journalpostId={}, eksternReferanseId={}) ukjent teknisk feil. melding={}",
					journalpostId, eksternReferanseId, e.getMessage(), e);
		}
	}
}
