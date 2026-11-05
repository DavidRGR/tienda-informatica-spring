package es.davidrgr.proyectoSpring.seguridad;

import es.davidrgr.proyectoSpring.modelo.Usuario;
import es.davidrgr.proyectoSpring.repositorio.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioDetailsService implements UserDetailsService {

	//Accedemos al usuarioRepository para poder trabajar con el
	private final UsuarioRepository usuarioRepository;

	public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	//Con este metodo cargamos los usuarios a partir de su correo
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
		//Buscamos al usuario por su correo y en caso de que falle lanzamos la excepción usuario no encontrado
		Usuario usuario = usuarioRepository.findByCorreo(correo).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

		//Con este metodo conseguimos una lista de autoridades (Los roles de usuario que utiliza y entiende Spring)
		//Con esto convertimos los roles en un stream para poder mapearlos
		List<SimpleGrantedAuthority> autoridades = usuario.getRoles().stream()
				//Los mapeamos y los convertimos a autoridades (Roles)
				.map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
				//Los pasamos a una lista
				.toList();

		//Retornamos el usuario con los campos necesarios para Spring para construirlo
		return new UsuarioAutenticado(usuario.getCorreo(), usuario.getPasswordHash(), autoridades, usuario.getNombre());
	}
}
