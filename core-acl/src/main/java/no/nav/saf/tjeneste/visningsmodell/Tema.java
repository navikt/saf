package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Tema {
	private final Temakode tema;
	private final String temanavn;

	public static Tema fromTemakode(Temakode temakode) {
		return Tema.builder()
				.tema(temakode)
				.temanavn(temakode.getTemanavn())
				.build();
	}
}
