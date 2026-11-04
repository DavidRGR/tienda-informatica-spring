package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.modelo.Categoria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria,Long> {

	@EntityGraph(attributePaths = "subcategorias")
	List<Categoria> findAllByOrderByNombreAsc();

}
