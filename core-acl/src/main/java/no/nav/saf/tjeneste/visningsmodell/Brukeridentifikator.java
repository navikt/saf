package no.nav.saf.tjeneste.visningsmodell;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.tjeneste.visningsmodell.kode.BrukeridentifikatorType;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Brukeridentifikator {

	private String ident;
	private BrukeridentifikatorType brukeridentifikatorType;

}
