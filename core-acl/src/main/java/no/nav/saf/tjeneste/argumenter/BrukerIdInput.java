package no.nav.saf.tjeneste.argumenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrukerIdInput {
	private String ident;
	private BrukerIdInputType identType;
}
