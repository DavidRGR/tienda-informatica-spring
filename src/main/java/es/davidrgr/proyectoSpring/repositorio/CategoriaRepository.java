package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.dto.CategoriaFila;
import es.davidrgr.proyectoSpring.modelo.Categoria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

//Hereda de la interfaz JpaRepository y se le pasan entre <> la clase Categoria y el tipo de su id (Long)
// Esto permite implementar metodos tales como:
// save(categoria): inserta o actualiza.
//findById(id): busca por clave primaria.
//findAll(): lista todos.
//deleteById(id) y count().
public interface CategoriaRepository extends JpaRepository<Categoria,Long> {

	@EntityGraph(attributePaths = "subcategorias")
	List<Categoria> findAllByOrderByNombreAsc();

	@Query("""
		select new es.davidrgr.proyectoSpring.dto.CategoriaFila(
			c.idCategoria, c.nombre, (select count(a) from Articulo a where a.categoria = c))
		from Categoria c
		order by lower(c.nombre)
		""")
	List<CategoriaFila> listarAdmin();

	boolean existsByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCaseAndIdCategoriaNot(String nombre, Long idCategoria);
}
