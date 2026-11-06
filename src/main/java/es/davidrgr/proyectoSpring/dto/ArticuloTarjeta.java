package es.davidrgr.proyectoSpring.dto;

import es.davidrgr.proyectoSpring.util.Imagenes;

import java.math.BigDecimal;

public record ArticuloTarjeta(Long id, String nombre, BigDecimal precio, String imagenUrl, int stock, String marca) {

	// Comprueba que tiene imagen
	public boolean tieneImagen() {
		return imagenUrl != null && !imagenUrl.isBlank();
	}

	// Devuelve la ruta lista para usar en el src de la imagen
	public String src() {
		return Imagenes.resolver(imagenUrl);
	}

}
