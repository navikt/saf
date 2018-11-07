package no.nav.saf.anticorruptionlayer.pensjonsak.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SakSammendragListeTo {

	List<SakSammendrag> sakSammendragListe;

	@Data
	@Builder
	public static class SakSammendrag {
		String sakNr;
		Arkivsakssystem arkivSakSystem;
		String tema;
		LocalDateTime datoOpprettet;
	}
}
