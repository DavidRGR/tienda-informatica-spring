package es.davidrgr.proyectoSpring.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoVista(List<LineaCarrito> lineas) {
	public boolean vacio() {
		return lineas.isEmpty();
	}

	public int unidades() {
		return lineas.stream().mapToInt(LineaCarrito::cantidad).sum();
	}

	public BigDecimal total() {
		return lineas.stream().map(LineaCarrito::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public boolean hayProblemasDeStock(){
		return lineas.stream().anyMatch(LineaCarrito::superaStock);
	}
}
