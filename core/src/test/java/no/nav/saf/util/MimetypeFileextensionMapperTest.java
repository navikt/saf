package no.nav.saf.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class MimetypeFileextensionMapperTest {

	@Test
	void shouldGetFileExtensionFromMediaType() {
		String fileextension = MimetypeFileextensionMapper.toFileextension(MediaType.APPLICATION_PDF);
		assertThat(fileextension, is(".pdf"));
	}

	@Test
	void shouldGetFileExtensionFromString() {
		String fileextension = MimetypeFileextensionMapper.toFileextension("application/pdf");
		assertThat(fileextension, is(".pdf"));
	}
}