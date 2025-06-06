package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSak;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import org.springframework.stereotype.Component;

import static java.lang.String.valueOf;
import static no.nav.saf.domain.kode.Arkivsakssystem.GSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static org.apache.commons.lang3.StringUtils.trim;

@Component
public class ArkivsakMapper {

	public static Arkivsak map(final JournalpostDto journalpostDto) {
		if (journalpostDto == null || journalpostDto.getSaksrelasjon() == null) {
			return null;
		}

		String aktoerId = null;
		String orgnummer = null;
		if (journalpostDto.getBruker() != null) {
			String brukerId = journalpostDto.getBruker().getBrukerId();
			if (journalpostDto.getBruker().isPerson()) {
				aktoerId = brukerId;
			} else {
				orgnummer = brukerId;
			}
		}

		SaksrelasjonDto saksrelasjonDto = journalpostDto.getSaksrelasjon();
		Arkivsakssystem arkivsaksystem = FagsystemCode.toSafArkivsaksystem(saksrelasjonDto.getFagsystem());
		return Arkivsak.builder()
				.arkivsaksnummer(saksrelasjonDto.getSakId())
				.arkivsaksystem(arkivsaksystem)
				.fagsakId(saksrelasjonDto.getFagsakNr())
				.fagsaksystem(saksrelasjonDto.getApplikasjon())
				.avsluttet(arkivsaksystem != PSAK && Arkivsak.sakStatusIsAvsluttet(saksrelasjonDto.getSakStatus()))
				.aktoerId(aktoerId)
				.orgnummer(orgnummer)
				.tema(FagomradeCode.toSafTema(journalpostDto.getFagomrade()))
				.build();
	}

	public static Arkivsak mapArkivsak(ArkivJournalpost arkivJournalpost) {
		ArkivSaksrelasjon arkivSaksrelasjon = arkivJournalpost.saksrelasjon();
		ArkivSak arkivSak = arkivSaksrelasjon.sak();
		return Arkivsak.builder()
				.arkivsaksnummer(valueOf(arkivSaksrelasjon.sakId()))
				.arkivsaksystem(arkivSaksrelasjon.isPensjonsak() ? PSAK : GSAK)
				.fagsakId(arkivSak.fagsakNr())
				.fagsaksystem(arkivSak.applikasjon())
				.avsluttet(!arkivSaksrelasjon.isPensjonsak() && Arkivsak.sakStatusIsAvsluttet(arkivSak.sakStatus()))
				.orgnummer(trim(arkivSak.orgNr()))
				.aktoerId(arkivSak.aktoerId())
				.tema(Arkivsak.mapTema(arkivSak.tema()))
				.build();
	}
}
