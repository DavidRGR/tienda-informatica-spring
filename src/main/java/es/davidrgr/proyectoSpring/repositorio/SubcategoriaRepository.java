package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.dto.SubcategoriaAdmin;
import es.davidrgr.proyectoSpring.modelo.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {
	boolean existsByIdSubcategoriaAndCategoriaIdCategoria(Long idSubcategoria, Long idCategoria);

	@Query("""
		select new es.davidrgr.proyectoSpring.dto.SubcategoriaAdmin(
			s.idSubcategoria, s.categoria.idCategoria, s.nombre,
			(select count(a) from Articulo a where a.subcategoria = s))
		from Subcategoria s
		order by lower(s.nombre)
		""")
	List<SubcategoriaAdmin> listarAdmin();

	boolean existsByCategoriaIdCategoriaAndNombreIgnoreCase(Long idCategoria, String nombre);

	boolean existsByCategoriaIdCategoriaAndNombreIgnoreCaseAndIdSubcategoriaNot(
			Long idCategoria, String nombre, Long idSubcategoria);

	long countByCategoriaIdCategoria(Long idCategoria);
}