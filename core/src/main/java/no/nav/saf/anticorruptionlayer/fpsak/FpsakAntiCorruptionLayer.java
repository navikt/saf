package no.nav.saf.anticorruptionlayer.fpsak;

import no.nav.saf.domain.Arkivsak;

import java.util.List;

public interface FpsakAntiCorruptionLayer {
	List<String> hentRelevanteParter(Arkivsak arkivsak);
}
