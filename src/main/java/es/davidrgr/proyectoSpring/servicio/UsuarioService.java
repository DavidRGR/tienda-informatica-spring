package es.davidrgr.proyectoSpring.servicio;

import es.davidrgr.proyectoSpring.dto.RegistroForm;
import es.davidrgr.proyectoSpring.modelo.Rol;
import es.davidrgr.proyectoSpring.modelo.Usuario;
import es.davidrgr.proyectoSpring.repositorio.RolRepository;
import es.davidrgr.proyectoSpring.repositorio.UsuarioRepository;
import es.davidrgr.proyectoSpring.seguridad.LimitadorIntentos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class UsuarioService {

	// Opciones que pueden salir al intentar registrar un usuario
	public enum ResultadoRegistro { CREADO, ROL_ANADIDO, PASSWORD_INCORRECTA, YA_TIENE_ROL, BLOQUEADO }

	//Inyeccion del limitador de intentos
	private final LimitadorIntentos limitador;

	// Accedemos a los repositorios de usuario y rol
	private final UsuarioRepository usuarioRepository;
	private final RolRepository rolRepository;

	// Accedemos al codificador de contraseñas
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder, LimitadorIntentos limitador) {
		this.usuarioRepository = usuarioRepository;
		this.rolRepository = rolRepository;
		this.passwordEncoder = passwordEncoder;
		this.limitador = limitador;
	}

	// Metodo para registrar o asignar Rol
	@Transactional
	public ResultadoRegistro registrarOAsignarRol(RegistroForm form, String nombreRol) {
		//Busca que el rol exista en la BBDD
		Rol rol = rolRepository.findByNombre(nombreRol)
				.orElseThrow(() -> new IllegalStateException("No existe el rol " + nombreRol + " en la base de datos"));

		// Busca al usuario por el correo
		String correo = form.getCorreo().trim().toLowerCase(Locale.ROOT);
		Optional<Usuario> existente = usuarioRepository.findByCorreo(correo);

		// Si existe
		if (existente.isPresent()) {
			// Le consigue
			Usuario usuario = existente.get();

			// Cuenta bloqueada: se rechaza incluso con la contraseña correcta
			if (limitador.estaBloqueado(correo)) {
				return ResultadoRegistro.BLOQUEADO;
			}

			// Solo el dueño de la cuenta puede añadirle un rol
			if (!passwordEncoder.matches(form.getPassword(), usuario.getPasswordHash())) {
				limitador.registrarFallo(correo);
				return ResultadoRegistro.PASSWORD_INCORRECTA;
			}
			limitador.reiniciar(correo);
			boolean yaTieneRol = usuario.getRoles().stream()
					.anyMatch(r -> r.getNombre().equals(nombreRol));
			if (yaTieneRol) {
				return ResultadoRegistro.YA_TIENE_ROL;
			}
			usuario.getRoles().add(rol);
			usuarioRepository.save(usuario);
			return ResultadoRegistro.ROL_ANADIDO;
		}

		Usuario nuevo = new Usuario(
				form.getNombre().trim(),
				correo,
				passwordEncoder.encode(form.getPassword()));
		nuevo.getRoles().add(rol);
		usuarioRepository.save(nuevo);
		return ResultadoRegistro.CREADO;
	}

}
