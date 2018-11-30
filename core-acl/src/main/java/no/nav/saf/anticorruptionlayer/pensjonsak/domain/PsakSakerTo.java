package no.nav.saf.anticorruptionlayer.pensjonsak.domain;

import lombok.Builder;
import lombok.Data;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;

import java.time.LocalDateTime;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class PsakSakerTo {
	String sakNr;
	Arkivsakssystem arkivSakSystem;
	String tema;
	LocalDateTime datoOpprettet;
}
