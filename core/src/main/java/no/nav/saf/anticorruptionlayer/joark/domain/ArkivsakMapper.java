package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.domain.Arkivsak;
import org.springframework.stereotype.Component;

/**
 * @author Erik Bråten, Visma Consulting
 */
@Component
public class ArkivsakMapper {

	public Arkivsak map(final JournalpostDto journalpostDto) {
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
		return Arkivsak.builder()
				.arkivsaksnummer(saksrelasjonDto.getSakId())
				.fagsaksystem(saksrelasjonDto.getFagsystem().name())
				.arkivsaksystem(FagsystemCode.toSafArkivsaksystem(saksrelasjonDto.getFagsystem()))
				.aktoerId(aktoerId)
				.orgnummer(orgnummer)
				.tema(FagomradeCode.toSafTema(journalpostDto.getFagomrade()))
				.build();
	}
}
