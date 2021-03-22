package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype;
import no.nav.saf.domain.kode.Arkivsakssystem;

import java.time.LocalDateTime;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Sak {
	/**
	 * @deprecated NB: Arkivsak skal anses som Joark-internt. Fagsystemene skal kun bruke denne etter avtale.
	 * @since 7.3.8
	 */
	@Deprecated(since = "7.3.8")
	private final String arkivsaksnummer;
	/**
	 * @deprecated NB: Arkivsak skal anses som Joark-internt. Fagsystemene skal kun bruke denne etter avtale.
	 * @since 7.3.8
	 */
	@Deprecated(since = "7.3.8")
	private final Arkivsakssystem arkivsaksystem;
	private final String fagsakId;
	private final String fagsaksystem;
	private final LocalDateTime datoOpprettet;
	private final Sakstype sakstype;
}
