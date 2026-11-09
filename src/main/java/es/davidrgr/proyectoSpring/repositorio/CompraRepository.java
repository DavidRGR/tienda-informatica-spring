package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.modelo.Compra;
import es.davidrgr.proyectoSpring.modelo.EstadoCompra;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {

	Optional<Compra> findFirstByUsuarioCorreoAndEstadoOrderByIdCompraAsc(String correo, EstadoCompra estado);

	Optional<Compra> findByIdCompraAndUsuarioCorreo(Long idCompra, String correo);

	// Bloquea la fila: dos "Finalizar" simultáneos no pueden cobrar el mismo carrito dos veces
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Compra c where c.usuario.correo = :correo and c.estado = :estado order by c.idCompra")
	List<Compra> bloquearPorEstado(@Param("correo") String correo, @Param("estado") EstadoCompra estado);

}
