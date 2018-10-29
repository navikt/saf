package no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.Temakode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
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
