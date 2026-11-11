package es.davidrgr.proyectoSpring.dto;

import java.util.List;

public record CategoriaAdmin(Long id, String nombre, Long articulos, List<SubcategoriaAdmin> subcategorias) {

	public boolean sePuedeEliminar() {
		return articulos == 0 && subcategorias.isEmpty();
	}
}
