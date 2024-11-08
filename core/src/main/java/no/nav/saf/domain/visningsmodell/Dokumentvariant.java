package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Variantformat;

import java.util.List;

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
	List<BrukerTilgangAvvistBegrunnelse> brukerTilgangAvvistBegrunnelser;
	boolean brukerHarTilgang;
}
