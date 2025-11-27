package no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenBulkResponse.Resultat;
import no.nav.saf.tilgangskontroll.pep.PepAnswer;
import no.nav.saf.tilgangskontroll.pep.reasons.EgenAnsattReason;
import no.nav.saf.tilgangskontroll.pep.reasons.FortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.GeografiReason;
import no.nav.saf.tilgangskontroll.pep.reasons.HabilitetReason;
import no.nav.saf.tilgangskontroll.pep.reasons.PersonUtlandReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseUtlandReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;

import static no.nav.saf.tilgangskontroll.pep.PepAnswer.deny;

@Slf4j
public class TilgangsmaskinenResponseMapper {

	private TilgangsmaskinenResponseMapper() {
	}

	public static PepAnswer map(TilgangsmaskinenDenyAnswer tilgangsmaskinenDenyAnswer, String pepName) {
		if (tilgangsmaskinenDenyAnswer == null) {
			return deny(new UkjentEllerTekniskReason());
		}
		if (tilgangsmaskinenDenyAnswer.status() == 204) {
			return PepAnswer.permit();
		}

		TilgangsmaskinenDenyReason reason = TilgangsmaskinenDenyReason.fromTitle(tilgangsmaskinenDenyAnswer.title());

		return switch (reason) {
			case AVVIST_HABILITET ->
					deny(new HabilitetReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse())); // informasjon om deg selv / familie
			case AVVIST_SKJERMING ->
					deny(new EgenAnsattReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse())); // informasjon om andre nav ansatte
			case AVVIST_GEOGRAFISK ->
					deny(new GeografiReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));
			case AVVIST_FORTROLIG_ADRESSE ->
					deny(new FortroligAdresseReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));
			case AVVIST_STRENGT_FORTROLIG_ADRESSE ->
					deny(new StrengtFortroligAdresseReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));
			case AVVIST_STRENGT_FORTROLIG_UTLAND ->
					deny(new StrengtFortroligAdresseUtlandReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));
			case AVVIST_PERSON_UTLAND ->
					deny(new PersonUtlandReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));

			default -> {
				log.warn("{} kunne ikke matche tilgangsmaskinen-response til DenyReason. title/avvisningskode={}", pepName, tilgangsmaskinenDenyAnswer.title());
				yield deny(new UkjentEllerTekniskReason(tilgangsmaskinenDenyAnswer.title(), tilgangsmaskinenDenyAnswer.begrunnelse()));
			}
		};
	}

	public static PepAnswer map(TilgangsmaskinenBulkResponse response, String pepName) {
		if (response == null || response.resultater() == null || response.resultater().isEmpty()) {
			return deny(new UkjentEllerTekniskReason());
		}

		return response.resultater().stream()
				.filter(Resultat::isDeny)
				.map(it -> map(it.detaljer(), pepName))
				.findFirst()
				.orElse(PepAnswer.permit());
	}
}
