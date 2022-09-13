package no.nav.saf.integration.penrest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record HentSakSammendragListeResponse (
		List<SakSammendrag> sakSammendragList
) {

	@JsonCreator
	public HentSakSammendragListeResponse(@JsonProperty("") List<SakSammendrag> sakSammendragList) {
		this.sakSammendragList = sakSammendragList;
	}
}
