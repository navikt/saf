package no.nav.saf.tjeneste.visningsmodell;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Dokumentoversikt {
	@Builder.Default
	private final List<Journalpost> journalposter = new ArrayList<>();
	private final SideInfo sideInfo;

	public static Dokumentoversikt empty() {
		return new Dokumentoversikt(new ArrayList<>(), null);
	}

	@JsonCreator
	public Dokumentoversikt(@JsonProperty("journalposter") List<Journalpost> journalposter,
							@JsonProperty("sideInfo") SideInfo sideInfo) {
		this.journalposter = journalposter;
		this.sideInfo = sideInfo;
	}
}
