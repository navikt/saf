package no.nav.saf.anticorruptionlayer.nav;

import java.util.List;
import java.util.stream.Stream;

record NavHrOrganisasjonORDSResponse(int count, List<NavHrOrganisasjon> items) {

	public Stream<NavHrOrganisasjon> organisasjoner() {
		return items.stream();
	}
}
