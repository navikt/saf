package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;

import java.util.List;

@Builder
public record ArkivUtsendingsInfo(
		ArkivDigitalPostadresse digitalPostadresse,
		List<ArkivEpostVarsel> epostVarsel,
		ArkivFysiskPostadresse fysiskPostadresse,
		Long journalpostId,
		ArkivNavNoVarsling navNoVarsling,
		List<ArkivSmsVarsel> smsVarsel
) {
}
