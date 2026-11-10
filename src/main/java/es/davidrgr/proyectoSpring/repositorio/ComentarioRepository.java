package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.dto.ComentarioVista;
import es.davidrgr.proyectoSpring.dto.ResumenValoracion;
import es.davidrgr.proyectoSpring.modelo.Comentario;
import es.davidrgr.proyectoSpring.modelo.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

	boolean existsByArticuloIdArticuloAndUsuarioCorreo(Long idArticulo, String correo);

	// La compra verificada se calcula: ¿tiene el autor una compra PAGADA con este artículo?
	@Query("""
			select new es.davidrgr.proyectoSpring.dto.ComentarioVista(
				c.idComentario, u.nombre, c.valoracion, c.texto, c.fecha,
				case when exists (
					select d.idDetalle from DetalleCompra d
					where d.compra.usuario = u and d.compra.estado = :pagada and d.articulo = c.articulo
				) then true else false end)
			from Comentario c
			join c.usuario u
			where c.articulo.idArticulo = :idArticulo and c.visible = true
			order by c.fecha desc, c.idComentario desc
			""")
	List<ComentarioVista> listarVisibles(@Param("idArticulo") Long idArticulo,
										 @Param("pagada") EstadoCompra pagada);

	@Query("""
			select new es.davidrgr.proyectoSpring.dto.ResumenValoracion(avg(c.valoracion), count(c))
			from Comentario c
			where c.articulo.idArticulo = :idArticulo and c.visible = true
			""")
	ResumenValoracion resumen(@Param("idArticulo") Long idArticulo);
}
