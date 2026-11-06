package es.davidrgr.proyectoSpring.dto;

import es.davidrgr.proyectoSpring.util.Imagenes;

import java.math.BigDecimal;

public record ArticuloDetalle(Long id, String nombre, String descripcion, BigDecimal precio,
							  String imagenUrl, int stock, String marca,
							  Long idCategoria, String categoria,
							  Long idSubcategoria, String subcategoria) {

	public String src() {
		return Imagenes.resolver(imagenUrl);
	}

	public boolean tieneSubcategoria() {
		return idSubcategoria != null;
	}

	public boolean tieneDescripcion() {
		return descripcion != null && !descripcion.isBlank();
	}

	// Máximo de unidades que se ofrecen en el selector de cantidad
	public int maxCantidad() {
		return Math.min(stock, 10);
	}

}
