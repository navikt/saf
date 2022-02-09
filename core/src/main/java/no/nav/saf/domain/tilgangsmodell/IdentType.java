package no.nav.saf.domain.tilgangsmodell;

import no.nav.saf.anticorruptionlayer.pdl.PdlResponse;

public enum IdentType {
	AKTOERID,
	FOLKEREGISTERIDENT,
	NPID;

	public static IdentType fromPdlGruppe(PdlResponse.PdlGruppe pdlGruppe) {
		if(PdlResponse.PdlGruppe.AKTORID.equals(pdlGruppe)) {
			return AKTOERID;
		}
		return IdentType.valueOf(pdlGruppe.name());
	}
}
