package es.davidrgr.proyectoSpring.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.print.DocFlavor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class AlmacenImagenes {

	private static final String PREFIJO = "/uploads/";

	private final Path directorio;
	private final DataSize maximo;

	public AlmacenImagenes(@Value("${app.uploads-dir:uploads}") String ruta,
						   @Value("${app.imagen-max:5MB}") DataSize maximo) throws IOException {
		this.directorio = Path.of(ruta).toAbsolutePath().normalize();
		this.maximo = maximo;
		Files.createDirectories(directorio);
	}

	public Path getDirectorio() {
		return directorio;
	}

	// El tipo se decide por los primeros bytes del archivo, no por el nombre ni por lo que diga el navegador
	private String extensionSegunContenido(MultipartFile archivo) throws IOException {
		byte[] c;
		try (InputStream in = archivo.getInputStream()) {
			c = in.readNBytes(12);
		}
		if (c.length >= 3 && (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF) {
			return "jpg";
		}
		if (c.length >= 8 && (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G') {
			return "png";
		}
		if (c.length >= 12 && c[0] == 'R' && c[1] == 'I' && c[2] == 'F' && c[3] == 'F'
				&& c[8] == 'W' && c[9] == 'E' && c[10] == 'B' && c[11] == 'P') {
			return "webp";
		}
		return null;
	}

	//Devuelve un mensaje de error, o null si el archivo es válido (O no se ha enviado ninguno)
	public String validar(MultipartFile archivo) {

		if (archivo == null || archivo.isEmpty()){
			return null;
		}
		if (archivo.getSize() > maximo.toBytes()) {
			return "La imagen no puede superar los " + maximo.toMegabytes() + " MB";
		}
		try {
			if (extensionSegunContenido(archivo) == null){
				return "Formato no válido: usa JPG, PNG o WebP";
			}
		}catch (IOException e) {
			return "No se ha podido leer la imagen";
		}
		return null;
	}

	// Guarda el archivo con un nombre generado y devuelve la ruta para la base de datos
	public String guardar(MultipartFile archivo) {
		try {
			String extension = extensionSegunContenido(archivo);
			if (extension == null) {
				throw new IllegalArgumentException("Formato de imagen no válido");
			}
			String nombre = UUID.randomUUID() + "." + extension;
			try (InputStream in = archivo.getInputStream()) {
				Files.copy(in, directorio.resolve(nombre));
			}
			return PREFIJO + nombre;
		}catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	// Solo borra archivos de esta carpeta. Las imágenes de static/img no se tocan
	public void borrar(String url) {
		if (url == null || !url.startsWith(PREFIJO)) {
			return;
		}
		Path nombre = Path.of(url.substring(PREFIJO.length())).getFileName();
		Path destino = directorio.resolve(nombre).normalize();
		if (!destino.startsWith(directorio)) {
			return;
		}
		try {
			Files.deleteIfExists(destino);
		} catch (IOException e) {
			// Un archivo que no se puede borrar no debe romper la operación
		}
	}
}
