package es.davidrgr.proyectoSpring.repositorio;

import es.davidrgr.proyectoSpring.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//Hereda de la interfaz JpaRepository y se le pasan entre <> la clase Usuario y el tipo de su id (Long)
// Esto permite implementar metodos tales como:
// save(usuario): inserta o actualiza.
//findById(id): busca por clave primaria.
//findAll(): lista todos.
//deleteById(id) y count().
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	//Metodo que permite comprobar si un usuario existe por su correo (En caso de existir devuelve True)
	boolean existsByCorreo(String correo);

	//Metodo derivado del findBy, se le pasa la clase en la que buscar y el parametro correo para que busque con él
	Optional<Usuario> findByCorreo(String correo);
}
