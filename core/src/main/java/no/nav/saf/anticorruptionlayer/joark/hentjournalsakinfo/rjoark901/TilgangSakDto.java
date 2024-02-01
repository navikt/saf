package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilgangSakDto {

	private String sakId;
	private String fagsystem;
	private String aktoerId;
	private String tema;
	private String fagsakNr;
	private String orgnr;
	private String applikasjon;
	private String opprettetAv;
	private ZonedDateTime opprettetTidspunkt;
}
