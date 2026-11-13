package es.davidrgr.proyectoSpring.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImagenesTest {

	@Test
	void sinImagenDevuelveLaDeRelleno() {
		assertThat(Imagenes.resolver(null)).isEqualTo("/img/sin-imagen.svg");
		assertThat(Imagenes.resolver("   ")).isEqualTo("/img/sin-imagen.svg");
	}

	@Test
	void unaUrlExternaSeRespeta() {
		assertThat(Imagenes.resolver("https://ejemplo.com/foto.jpg")).isEqualTo("https://ejemplo.com/foto.jpg");
	}

	@Test
	void unaRutaAbsolutaSeRespeta() {
		assertThat(Imagenes.resolver("/uploads/abc.png")).isEqualTo("/uploads/abc.png");
	}

	@Test
	void unaRutaRelativaSeBuscaEnStaticImg() {
		assertThat(Imagenes.resolver("categorias/2.jpg")).isEqualTo("/img/categorias/2.jpg");
	}
}
