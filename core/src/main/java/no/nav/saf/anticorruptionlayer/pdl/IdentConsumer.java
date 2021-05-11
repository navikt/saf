package no.nav.saf.anticorruptionlayer.pdl;

import java.util.List;

public interface IdentConsumer {
	/**
	 * Henter alle identer NAV har på en bruker.
	 *
	 * @param ident Ident tilhørende person
	 * @return NAV identer. Både folkeregisteridenter og aktørid.
	 * @throws PersonIkkeFunnetException Finner ikke person
	 */
	List<PdlResponse.PdlIdent> hentIdenter(final String ident) throws PersonIkkeFunnetException;
}
