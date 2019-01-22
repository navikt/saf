package no.nav.saf.anticorruptionlayer.bisys.hentRelevanteTredjeparterOgParagraf19;

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
