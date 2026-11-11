package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.dto.MarcaAdmin;
import es.davidrgr.proyectoSpring.modelo.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
	List<Marca> findAllByOrderByNombreAsc();

	@Query("""
		select new es.davidrgr.proyectoSpring.dto.MarcaAdmin(
			m.idMarca, m.nombre, (select count(a) from Articulo a where a.marca = m))
		from Marca m
		order by lower(m.nombre)
		""")
	List<MarcaAdmin> listarAdmin();

	boolean existsByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCaseAndIdMarcaNot(String nombre, Long idMarca);
}
