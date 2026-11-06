package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.dto.ArticuloTarjeta;
import es.davidrgr.proyectoSpring.modelo.Articulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticuloRepository extends JpaRepository<Articulo, Long> {

	String TARJETA = "select new es.davidrgr.proyectoSpring.dto.ArticuloTarjeta("
			+ "a.idArticulo, a.nombre, a.precio, a.imagenUrl, a.stock, a.marca.nombre) from Articulo a ";

	@Query(value = TARJETA + "where a.activo = true",
			countQuery = "select count(a) from Articulo a where a.activo = true")
	Page<ArticuloTarjeta> listarActivos(Pageable pageable);

	@Query(value = TARJETA + "where a.activo = true and a.categoria.idCategoria = :id",
			countQuery = "select count(a) from Articulo a where a.activo = true and a.categoria.idCategoria = :id")
	Page<ArticuloTarjeta> listarPorCategoria(@Param("id") Long id, Pageable pageable);

	@Query(value = TARJETA + "where a.activo = true and a.subcategoria.idSubcategoria = :id",
			countQuery = "select count(a) from Articulo a where a.activo = true and a.subcategoria.idSubcategoria = :id")
	Page<ArticuloTarjeta> listarPorSubcategoria(@Param("id") Long id, Pageable pageable);
}
