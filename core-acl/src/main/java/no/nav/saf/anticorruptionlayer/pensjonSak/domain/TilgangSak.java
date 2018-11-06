package no.nav.saf.anticorruptionlayer.pensjonSak.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilgangSak {
	String sakNr;
	Arkivsakssystem arkivSakSystem;
	String tema;
}
