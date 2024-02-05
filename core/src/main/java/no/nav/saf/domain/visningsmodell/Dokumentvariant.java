package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Variantformat;

@Value
@Builder
public class Dokumentvariant {
	Variantformat variantformat;
	String filnavn;
	String filuuid;
	String filtype;
	Integer filstoerrelse;
	boolean saksbehandlerHarTilgang;
	Skjerming skjerming;
}
