package no.nav.saf.anticorruptionlayer.pensjonsak.hentBrukerOgEnhetstilgangerForSak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HentBrukerForSakResponseTo {
	private String fnr;
}
