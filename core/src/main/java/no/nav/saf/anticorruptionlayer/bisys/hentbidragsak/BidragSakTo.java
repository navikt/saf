package no.nav.saf.anticorruptionlayer.bisys.hentbidragsak;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;

@Value
@Builder
public class BidragSakTo {
	private final String saksnummer;
	private final boolean erParagraf19;
	private final ArrayList<String> roller;
}
