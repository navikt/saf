package no.nav.saf.anticorruptionlayer.nav.entraproxy;

import no.nav.saf.domain.kode.Tema;

import java.util.Set;

public record EntraProxyTematilgangResponse(Set<String> temaer) {

	public boolean harTilgangTilTema(Tema tema) {
		return temaer.contains(tema.name());
	}
}
