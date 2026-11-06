package es.davidrgr.proyectoSpring.dto;

import java.math.BigDecimal;

public record ArticuloTarjeta(Long id, String nombre, BigDecimal precio, String imagenUrl, int stock, String marca) {

	// Comprueba que tiene imagen
	public boolean tieneImagen() {
		return imagenUrl != null && !imagenUrl.isBlank();
	}

	// Devuelve la ruta lista para usar en el src de la imagen
	public String src() {
		if (!tieneImagen()) {
			return "/img/sin-imagen.svg";
		}
		String url = imagenUrl.trim();

		if (url.startsWith("http") || url.startsWith("/")) {
			// URL externa o absoluta
			return url;
		}
		// Imagenes del proyecto
		// nombre de archivo en static/img
		return "/img/" + url;
	}

}
