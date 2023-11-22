package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.BrukerTypeCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrukerDto {
	private String brukerId;
	private String brukerIdType;

	public boolean isPerson() {
		return BrukerTypeCode.PERSON.equals(brukerIdType);
	}

	public boolean isOrganisasjon() {
		return BrukerTypeCode.ORGANISASJON.equals(brukerIdType);
	}
}
