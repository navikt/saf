package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyConsumer;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyTematilgangResponse;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.TemaReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.kode.Tema.FAR;
import static no.nav.saf.domain.kode.Tema.KTA;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

@Slf4j
@Component(PEP2)
public class EntraProxyBackedPep2Impl extends StandardEntraProxyBackedPep<TilgangSak> {

	private static final EnumSet<Tema> relevanteTema = EnumSet.of(FAR, KTA);
	private static final String MANGLER_DATA_TEKNISK_FEILMELDING = "Pep2 (tema FAR eller KTA) mangler data om journalposten. Den må ha tema for å gjøre tilgangskontroll. Dette er forårsaket av en teknisk feil";

	private final EntraProxyConsumer entraProxyConsumer;

	public EntraProxyBackedPep2Impl(EntraProxyConsumer entraProxyConsumer) {
		this.entraProxyConsumer = entraProxyConsumer;
	}


	@Override
	PepAnswer verifyNavIdentAccessToTema(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.error(MANGLER_DATA_TEKNISK_FEILMELDING);
			return PepAnswer.deny(new UkjentEllerTekniskReason());
		}

		Tema tema = ressurs.getTema();

		if (relevanteTema.contains(tema)) {
			EntraProxyTematilgangResponse tematilgangResponse = entraProxyConsumer.hentTematilgangForNavAnsatt(safRequestContext);

			if (tematilgangResponse.harTilgangTilTema(tema)) {
				return PepAnswer.permit();
			}

			return getDenyAnswerForTema(tema);
		}


		return PepAnswer.permit();
	}

	@Override
	PepAnswer verifyAccessForSystemUser(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.error(MANGLER_DATA_TEKNISK_FEILMELDING);
			return PepAnswer.deny(new UkjentEllerTekniskReason());
		}

		Tema tema = ressurs.getTema();

		if (relevanteTema.contains(tema)) {
			return safRequestContext.getSecurityContext().hasJournalTilgangEntraRole(tema) ? permit() : getDenyAnswerForTema(tema);
		} else {
			return permit();
		}
	}

	private PepAnswer getDenyAnswerForTema(Tema tema) {
		var policy = switch (tema) {
			case FAR -> "saf_farskap";
			case KTA -> "saf_kontrollanmeldelse";
			default -> "saf_pep2";
		};

		return PepAnswer.deny(new TemaReason(
				"cause_0013_ikketilgangtilJournaltema",
				policy,
				"tematilgang_nok",
				tema));
	}
}
