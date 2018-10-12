package no.nav.saf.context.saf.domain;

import lombok.Builder;
import lombok.Data;
import no.nav.saf.context.saf.domain.kode.Temakode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class Tema {
	private final Temakode tema;
	private final String temanavn;
	@Builder.Default
	private final List<Sak> saker = new ArrayList<>();

	public static Tema fromTemakode(Temakode temakode) {
		return Tema.builder()
				.tema(temakode)
				.temanavn(temakode.getTemanavn())
				.build();
	}
}
