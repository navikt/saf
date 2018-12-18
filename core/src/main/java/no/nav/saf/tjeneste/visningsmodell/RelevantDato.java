package no.nav.saf.tjeneste.visningsmodell;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.Datotype;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
//@Value
	@Data
@AllArgsConstructor
public class RelevantDato {
	// Fallback for datoer som er påkrevd men av ukjente årsaker ikke finnes.
	public static final LocalDateTime INVALID_DATE = LocalDateTime.of(LocalDate.of(1, 1, 1), LocalTime.of(0, 0));

	private final LocalDateTime dato;
	private final Datotype datotype;

	public RelevantDato(Date dato, Datotype datotype) {
		this.dato = toLocalDateTime(dato);
		this.datotype = datotype;
	}

	private static LocalDateTime toLocalDateTime(Date date) {
		if(date == null) {
			return INVALID_DATE;
		}
		return LocalDateTime.from(date.toInstant().atZone(ZoneId.systemDefault()));
	}
}
