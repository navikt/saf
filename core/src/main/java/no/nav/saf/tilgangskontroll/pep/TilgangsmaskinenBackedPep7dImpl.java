package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenConsumer;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_FORELDREPENGELOSNING;
import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_K9;
import static no.nav.saf.domain.DomainConstants.PEP7D;
import static no.nav.saf.domain.kode.Tema.FOR;
import static no.nav.saf.domain.kode.Tema.FRI;
import static no.nav.saf.domain.kode.Tema.OMS;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

@Slf4j
@Component(PEP7D)
public class TilgangsmaskinenBackedPep7dImpl extends StandardTilgangsmaskinenBackedPep<TilgangSak> {

	private static final EnumSet<Tema> relevanteTemaK9 = EnumSet.of(FRI, OMS);

	private final TilgangsmaskinenConsumer tilgangsmaskinenConsumer;

	public TilgangsmaskinenBackedPep7dImpl(TilgangsmaskinenConsumer tilgangsmaskinenConsumer) {
		this.tilgangsmaskinenConsumer = tilgangsmaskinenConsumer;
	}

	@Override
	PepAnswer verifyNavIdentAccessToUser(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (harIkkeArkivsaksystemEllerArkivsaksummer(ressurs)) {
			return PepAnswer.permit();
		}

		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep7d(ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer());

		if (erFagsystemForeldrepenger(ressurs)) {
			return handterTilgangForFagsaksystem(ressurs.getFpAktoerIdList(), "foreldrepengesak", safRequestContext, tilgangKeyLocalCaching);
		}

		if (erFagsystemK9(ressurs)) {
			return handterTilgangForFagsaksystem(ressurs.getK9AktoerIdList(), "K9sak", safRequestContext, tilgangKeyLocalCaching);
		}

		safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
		return PepAnswer.permit();
	}

	private PepAnswer handterTilgangForFagsaksystem(List<String> parter, String fagsaksystemNavn, SafRequestContext safRequestContext, String tilgangKeyLocalCaching) {
		if (parter == null || parter.isEmpty()) {
			log.info("Pep7d(kode6/7-relevante-parter) har ingen relevante parter for {}. Tilgang gis.", fagsaksystemNavn);
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
			return PepAnswer.permit();
		}

		if (safRequestContext.getRequestCache().getCachedDecision(tilgangKeyLocalCaching) == null) {
			PepAnswer pepAnswer = evaluerTilgangForParter(parter, safRequestContext);
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
			return pepAnswer;
		}

		return safRequestContext.getRequestCache().getCachedDecision(tilgangKeyLocalCaching);
	}

	private PepAnswer evaluerTilgangForParter(List<String> parter, SafRequestContext safRequestContext) {
		if (parter.size() == 1) {
			return tilgangsmaskinenConsumer.navIdentHasAccess(parter.getFirst(), safRequestContext, PEP7D);
		} else {
			return tilgangsmaskinenConsumer.navIdentHasAccessBulk(parter, safRequestContext, PEP7D);
		}
	}

	private static boolean erFagsystemK9(TilgangSak ressurs) {
		return relevanteTemaK9.contains(ressurs.getTema()) && FAGSAKSYSTEM_K9.equals(ressurs.getFagsaksystem());
	}

	private static boolean erFagsystemForeldrepenger(TilgangSak ressurs) {
		return FOR.equals(ressurs.getTema()) && FAGSAKSYSTEM_FORELDREPENGELOSNING.equals(ressurs.getFagsaksystem());
	}

	private static boolean harIkkeArkivsaksystemEllerArkivsaksummer(TilgangSak ressurs) {
		return ressurs == null
				|| ressurs.getArkivsaksystem() == null
				|| ressurs.getArkivsaksnummer() == null;
	}

	@Override
	PepAnswer verifyAccessForSystem(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (!harIkkeArkivsaksystemEllerArkivsaksummer(ressurs)) {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep7d(ressurs.getArkivsaksystem(), ressurs.getArkivsaksnummer());
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
		}
		return permit();
	}
}
