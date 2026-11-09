package es.davidrgr.proyectoSpring.dto;

import es.davidrgr.proyectoSpring.util.Imagenes;

import java.math.BigDecimal;

public record LineaCarrito(Long idDetalle, Long idArticulo, String nombre, String marca, String imagenUrl, int cantidad, int stock, BigDecimal precioUnitario){

	public String src() {
		return Imagenes.resolver(imagenUrl);
	}

	public BigDecimal subtotal() {
		return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
	}

	public boolean superaStock() {
		return cantidad > stock;
	}
}
