package no.nav.saf.domain;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class Bruker {
	private final String fnr;
	private final String aktoerId;
	@Builder.Default
	private final Set<Tema> temaer = new HashSet<>();
}
