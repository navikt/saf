package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark902;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrukerDto {
	private String brukerId;
	private String brukerType;
}
