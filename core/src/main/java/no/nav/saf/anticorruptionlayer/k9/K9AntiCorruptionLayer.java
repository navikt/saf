package no.nav.saf.anticorruptionlayer.k9;

import no.nav.saf.domain.Arkivsak;

import java.util.List;

public interface K9AntiCorruptionLayer {
	List<String> hentRelevanteParter(Arkivsak arkivsak);
}
