package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.modelo.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//Hereda de la interfaz JpaRepository y se le pasan entre <> la clase Rol y el tipo de su id (Long)
// Esto permite implementar metodos tales como:
// save(Rol): inserta o actualiza.
//findById(id): busca por clave primaria.
//findAll(): lista todos.
//deleteById(id) y count().
public interface RolRepository extends JpaRepository<Rol, Long> {
	//Permite buscar roles por el nombre
	Optional<Rol> findByNombre(String nombre);
}
