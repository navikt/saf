package no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen;

import no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenBulkResponse.Resultat;
import no.nav.saf.tilgangskontroll.pep.reasons.DenyReason;
import no.nav.saf.tilgangskontroll.pep.reasons.EgenAnsattReason;
import no.nav.saf.tilgangskontroll.pep.reasons.FortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.GeografiReason;
import no.nav.saf.tilgangskontroll.pep.reasons.HabilitetReason;
import no.nav.saf.tilgangskontroll.pep.reasons.PersonUtlandReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseReason;
import no.nav.saf.tilgangskontroll.pep.reasons.StrengtFortroligAdresseUtlandReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenDenyReason.AVVIST_FORTROLIG_ADRESSE;
import static no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenDenyReason.AVVIST_GEOGRAFISK;
import static no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenDenyReason.AVVIST_HABILITET;
import static no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenDenyReason.AVVIST_PERSON_UTLAND;
import static no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenDenyReason.AVVIST_SKJERMING;
import static no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenDenyReason.AVVIST_STRENGT_FORTROLIG_ADRESSE;
import static no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenDenyReason.AVVIST_STRENGT_FORTROLIG_UTLAND;
import static no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenDenyReason.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

class TilgangsmaskinenResponseMapperTest {

	@Test
	void shouldMapToPermitWhenStatusIs204() {
		var result = TilgangsmaskinenResponseMapper.map(createDenyAnswerWithPermit(), "pep");
		assertThat(result.isPermit()).isTrue();
	}

	@Test
	void shouldMapToDenyWhenAnswerIsNull() {
		var result = TilgangsmaskinenResponseMapper.map((TilgangsmaskinenDenyAnswer) null, "pep");
		assertThat(result.isDeny()).isTrue();
	}

	@ParameterizedTest
	@MethodSource
	void shouldMapToDenyForAllKnownReasons(TilgangsmaskinenDenyAnswer denyAnswer, Class<?> expected) {
		var result = TilgangsmaskinenResponseMapper.map(denyAnswer, "pep");

		assertThat(result.isDeny()).isTrue();

		assertThat(result.getPepDenyReason())
				.isInstanceOf(expected)
				.extracting(DenyReason::getRawTilgangsmaskinenDenyReason, DenyReason::getRawTilgangsmaskinenBegrunnelse)
				.containsExactly(denyAnswer.title(), denyAnswer.begrunnelse());
	}

	static Stream<Arguments> shouldMapToDenyForAllKnownReasons() {
		return Stream.of(
				Arguments.of(createDenyAnswer(AVVIST_HABILITET.name()), HabilitetReason.class),
				Arguments.of(createDenyAnswer(AVVIST_SKJERMING.name()), EgenAnsattReason.class),
				Arguments.of(createDenyAnswer(AVVIST_GEOGRAFISK.name()), GeografiReason.class),
				Arguments.of(createDenyAnswer(AVVIST_FORTROLIG_ADRESSE.name()), FortroligAdresseReason.class),
				Arguments.of(createDenyAnswer(AVVIST_STRENGT_FORTROLIG_ADRESSE.name()), StrengtFortroligAdresseReason.class),
				Arguments.of(createDenyAnswer(AVVIST_STRENGT_FORTROLIG_UTLAND.name()), StrengtFortroligAdresseUtlandReason.class),
				Arguments.of(createDenyAnswer(AVVIST_PERSON_UTLAND.name()), PersonUtlandReason.class),
				Arguments.of(createDenyAnswer(UNKNOWN.name()), UkjentEllerTekniskReason.class)
		);
	}

	@Test
	void shouldMapBulkToDenyWhenAnyDenyExists() {
		TilgangsmaskinenBulkResponse response = new TilgangsmaskinenBulkResponse(
				"ansattId",
				List.of(new Resultat("brukerId1", 204, null),
						new Resultat("brukerId2", 403, createDenyAnswer(AVVIST_HABILITET.name())
						)));

		var result = TilgangsmaskinenResponseMapper.map(response, "pep");

		assertThat(result.isDeny()).isTrue();
	}

	@Test
	void shouldMapBulkToPermitWhenAllPermit() {
		TilgangsmaskinenBulkResponse response = new TilgangsmaskinenBulkResponse(
				"ansattId",
				List.of(new Resultat("12345678910", 204, null),
						new Resultat("12345678910", 204, null)
				));

		var result = TilgangsmaskinenResponseMapper.map(response, "pep");

		assertThat(result.isPermit()).isTrue();
	}

	private static TilgangsmaskinenDenyAnswer createDenyAnswerWithPermit() {
		return createDenyAnswer("title", 204);
	}

	private static TilgangsmaskinenDenyAnswer createDenyAnswer(String title) {
		return createDenyAnswer(title, 403);
	}

	private static TilgangsmaskinenDenyAnswer createDenyAnswer(String title, int status) {
		return new TilgangsmaskinenDenyAnswer(
				"type",
				title,
				status,
				"instance",
				"brukerIdent",
				"navIdent",
				"Avvist pga %s".formatted(title),
				"traceId",
				false
		);
	}

}