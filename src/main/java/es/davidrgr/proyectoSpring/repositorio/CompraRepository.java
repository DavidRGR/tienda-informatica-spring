package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.dto.ComentarioAdmin;
import es.davidrgr.proyectoSpring.dto.CompraResumen;
import es.davidrgr.proyectoSpring.modelo.Compra;
import es.davidrgr.proyectoSpring.modelo.EstadoCompra;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {

	Optional<Compra> findFirstByUsuarioCorreoAndEstadoOrderByIdCompraAsc(String correo, EstadoCompra estado);

	Optional<Compra> findByIdCompraAndUsuarioCorreo(Long idCompra, String correo);

	// Bloquea la fila: dos "Finalizar" simultáneos no pueden cobrar el mismo carrito dos veces
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Compra c where c.usuario.correo = :correo and c.estado = :estado order by c.idCompra")
	List<Compra> bloquearPorEstado(@Param("correo") String correo, @Param("estado") EstadoCompra estado);

	@Query("""
		select new es.davidrgr.proyectoSpring.dto.CompraResumen(
			c.idCompra, c.fecha, c.total, c.estado, coalesce(sum(d.cantidad), 0L))
		from Compra c
		left join c.detalles d
		where c.usuario.correo = :correo and c.estado <> :excluido
		group by c.idCompra, c.fecha, c.total, c.estado
		order by c.fecha desc, c.idCompra desc
		""")
	List<CompraResumen> resumenes(@Param("correo") String correo, @Param("excluido") EstadoCompra excluido);

	// Bloquea la compra para que dos cancelaciones simultáneas no devuelvan el stock dos veces
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Compra c where c.idCompra = :id and c.usuario.correo = :correo")
	Optional<Compra> bloquearPropia(@Param("id") Long id, @Param("correo") String correo);
}
