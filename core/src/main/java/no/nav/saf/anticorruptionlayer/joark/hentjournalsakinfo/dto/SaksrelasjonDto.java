package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class SaksrelasjonDto {
	private String sakId;
	private Boolean feilregistrert;
	private FagsystemCode fagsystem;
	private final String aktoerId;
	private final String tema;
	private final String fagsakNr;
	private final String applikasjon;
	private final String orgnr;
	private final String opprettetAv;
	private final String sakStatus;
	private final OffsetDateTime opprettetTidspunkt;
}
