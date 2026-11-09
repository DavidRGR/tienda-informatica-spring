package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.dto.LineaCarrito;
import es.davidrgr.proyectoSpring.modelo.DetalleCompra;
import es.davidrgr.proyectoSpring.modelo.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

	String LINEA = "select new es.davidrgr.proyectoSpring.dto.LineaCarrito("
			+ "d.idDetalle, a.idArticulo, a.nombre, m.nombre, a.imagenUrl, d.cantidad, a.stock, ";

	// Carrito: se muestra el precio ACTUAL del artículo
	@Query("""
			select new es.davidrgr.proyectoSpring.dto.LineaCarrito(
				d.idDetalle, a.idArticulo, a.nombre, m.nombre, a.imagenUrl, d.cantidad, a.stock, a.precio)
			from DetalleCompra d
			join d.articulo a
			join a.marca m
			where d.compra.usuario.correo = :correo and d.compra.estado = :estado
			order by d.idDetalle
			""")
	List<LineaCarrito> lineasDelCarrito(@Param("correo") String correo, @Param("estado") EstadoCompra estado);

	// Compra ya hecha: se muestra el precio al que se compró
	@Query("""
			select new es.davidrgr.proyectoSpring.dto.LineaCarrito(
				d.idDetalle, a.idArticulo, a.nombre, m.nombre, a.imagenUrl, d.cantidad, a.stock, d.precioUnitario)
			from DetalleCompra d
			join d.articulo a
			join a.marca m
			where d.compra.idCompra = :idCompra and d.compra.usuario.correo = :correo
			order by d.idDetalle
			""")
	List<LineaCarrito> lineasDeCompra(@Param("idCompra") Long idCompra, @Param("correo") String correo);

	// Solo devuelve la línea si es del carrito pendiente de ESE usuario
	Optional<DetalleCompra> findByIdDetalleAndCompraUsuarioCorreoAndCompraEstado(
			Long idDetalle, String correo, EstadoCompra estado);

	@Query("""
			select coalesce(sum(d.cantidad), 0L)
			from DetalleCompra d
			where d.compra.usuario.correo = :correo and d.compra.estado = :estado
			""")
	long unidadesEnCarrito(@Param("correo") String correo, @Param("estado") EstadoCompra estado);
}
