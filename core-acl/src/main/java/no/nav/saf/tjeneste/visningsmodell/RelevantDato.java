package no.nav.saf.tjeneste.visningsmodell;

import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.Datotype;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class RelevantDato {
	private final LocalDateTime dato;
	private final Datotype datotype;

	public RelevantDato(Date dato, Datotype datotype) {
		this.dato = toLocalDateTime(dato);
		this.datotype = datotype;
	}

	private static LocalDateTime toLocalDateTime(Date date) {
		if(date == null) {
			return null;
		}
		return LocalDateTime.from(date.toInstant().atZone(ZoneId.systemDefault()));
	}
}
