package no.nav.saf.anticorruptionlayer.pensjonsak.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.informasjon.WSSakSammendrag;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SakSammendragListeTo {
	List<WSSakSammendrag> sakSammendragListe;
}
