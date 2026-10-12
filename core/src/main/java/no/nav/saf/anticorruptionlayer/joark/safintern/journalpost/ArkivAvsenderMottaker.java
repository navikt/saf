package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.AvsenderMottakerIdTypeCode.ORGNR;

public record ArkivAvsenderMottaker(
		String id,
		String type,
		String navn,
		String land
) {
	public boolean erOrganisasjon() {
		return ORGNR.name().equals(type);
	}
}
