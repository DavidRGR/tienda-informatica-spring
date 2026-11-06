package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.dto.ArticuloDetalle;
import es.davidrgr.proyectoSpring.dto.ArticuloTarjeta;
import es.davidrgr.proyectoSpring.modelo.Articulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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
	@Query("""
		select new es.davidrgr.proyectoSpring.dto.ArticuloDetalle(
			a.idArticulo, a.nombre, a.descripcion, a.precio, a.imagenUrl, a.stock,
			m.nombre, c.idCategoria, c.nombre, s.idSubcategoria, s.nombre)
		from Articulo a
		join a.marca m
		join a.categoria c
		left join a.subcategoria s
		where a.idArticulo = :id and a.activo = true
		""")
	Optional<ArticuloDetalle> buscarDetalle(@Param("id") Long id);

}
