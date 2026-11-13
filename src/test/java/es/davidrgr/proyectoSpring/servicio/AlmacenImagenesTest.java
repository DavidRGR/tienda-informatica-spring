package es.davidrgr.proyectoSpring.servicio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlmacenImagenesTest {

	private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};
	private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
	private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

	@TempDir
	Path carpeta;

	private AlmacenImagenes almacen;

	@BeforeEach
	void preparar() throws IOException {
		// Si tu constructor solo recibe la ruta, quita el segundo argumento
		almacen = new AlmacenImagenes(carpeta.toString(), DataSize.ofMegabytes(5));
	}

	private MockMultipartFile archivo(String nombre, byte[] contenido) {
		return new MockMultipartFile("imagen", nombre, "image/jpeg", contenido);
	}

	// ---------------- Validación ----------------
	@Test
	void sinArchivoEsValido() {
		assertThat(almacen.validar(null)).isNull();
		assertThat(almacen.validar(archivo("vacio.jpg", new byte[0]))).isNull();
	}

	@Test
	void aceptaJpgPngYWebp() {
		assertThat(almacen.validar(archivo("a.jpg", JPG))).isNull();
		assertThat(almacen.validar(archivo("a.png", PNG))).isNull();
		assertThat(almacen.validar(archivo("a.webp", WEBP))).isNull();
	}

	@Test
	void rechazaUnPdfAunqueSeLlameJpg() {
		byte[] pdf = "%PDF-1.4 contenido".getBytes(StandardCharsets.UTF_8);
		assertThat(almacen.validar(archivo("foto.jpg", pdf))).contains("Formato no válido");
	}

	@Test
	void rechazaSvgPorqueSePuedeUsarParaScripts() {
		byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
				.getBytes(StandardCharsets.UTF_8);
		assertThat(almacen.validar(archivo("dibujo.svg", svg))).contains("Formato no válido");
	}

	@Test
	void rechazaLosArchivosQueSuperanElMaximo() {
		byte[] grande = new byte[5 * 1024 * 1024 + 1];
		System.arraycopy(JPG, 0, grande, 0, JPG.length);
		assertThat(almacen.validar(archivo("grande.jpg", grande))).contains("5 MB");
	}

	// ---------------- Guardado ----------------
	@Test
	void guardaConNombreGeneradoYExtensionSegunElContenido() throws IOException {
		// El nombre que envía el usuario no se usa, ni la extensión que declara
		String ruta = almacen.guardar(archivo("../../evil.php", PNG));

		assertThat(ruta).startsWith("/uploads/").endsWith(".png").doesNotContain("evil");
		Path guardado = carpeta.resolve(ruta.substring("/uploads/".length()));
		assertThat(guardado).exists();
	}

	@Test
	void noGuardaUnArchivoQueNoEsUnaImagen() {
		MockMultipartFile pdf = archivo("foto.jpg", "%PDF-1.4".getBytes(StandardCharsets.UTF_8));
		assertThatThrownBy(() -> almacen.guardar(pdf)).isInstanceOf(IllegalArgumentException.class);
	}

	// ---------------- Borrado ----------------
	@Test
	void soloBorraArchivosDeLaCarpetaDeSubidas() throws IOException {
		Path dentro = Files.createFile(carpeta.resolve("a.jpg"));

		almacen.borrar("categorias/a.jpg"); // imagen de static: no se toca
		assertThat(dentro).exists();

		almacen.borrar("/uploads/a.jpg");
		assertThat(dentro).doesNotExist();
	}

	@Test
	void noBorraArchivosFueraDeLaCarpetaAunqueSeIntenteSalirDeElla() throws IOException {
		Path fuera = Files.createFile(carpeta.getParent().resolve("fuera-" + UUID.randomUUID() + ".txt"));
		try {
			almacen.borrar("/uploads/../" + fuera.getFileName());
			assertThat(fuera).exists();
		} finally {
			Files.deleteIfExists(fuera);
		}
	}

	@Test
	void borrarUnaRutaNulaNoFalla() {
		almacen.borrar(null);
	}
}
