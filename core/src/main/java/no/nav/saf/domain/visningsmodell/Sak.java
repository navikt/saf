package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;

import java.time.LocalDateTime;

import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;

@Value
@Builder
public class Sak {
	/**
	 * @deprecated NB: Arkivsak skal anses som Joark-internt. Fagsystemene skal kun bruke denne etter avtale.
	 * @since 7.3.8
	 */
	@Deprecated(since = "7.3.8")
	String arkivsaksnummer;
	/**
	 * @deprecated NB: Arkivsak skal anses som Joark-internt. Fagsystemene skal kun bruke denne etter avtale.
	 * @since 7.3.8
	 */
	@Deprecated(since = "7.3.8")
	Arkivsakssystem arkivsaksystem;
	String fagsakId;
	String fagsaksystem;
	LocalDateTime datoOpprettet;
	Sakstype sakstype;
	Tema tema;

	public boolean isPsak() {
		return PSAK.equals(arkivsaksystem);
	}
}
