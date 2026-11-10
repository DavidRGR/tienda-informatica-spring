package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.modelo.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {
	boolean existsByIdSubcategoriaAndCategoriaIdCategoria(Long idSubcategoria, Long idCategoria);
}