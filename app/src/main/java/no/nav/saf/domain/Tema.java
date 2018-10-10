package no.nav.saf.domain;

import lombok.Builder;
import lombok.Data;
import no.nav.saf.domain.kode.Temakode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class Tema {
	private final Temakode tema;
	@Builder.Default
	private final List<Sak> saker = new ArrayList<>();
}
