package no.nav.saf.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidragSak {
	@Builder.Default
	private final List<TilgangRelevantTredjepart> relevanteTredjeparter = new ArrayList<>();
}
