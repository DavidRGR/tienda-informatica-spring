package es.davidrgr.proyectoSpring.servicio;

import es.davidrgr.proyectoSpring.dto.RegistroForm;
import es.davidrgr.proyectoSpring.modelo.Rol;
import es.davidrgr.proyectoSpring.modelo.Usuario;
import es.davidrgr.proyectoSpring.repositorio.RolRepository;
import es.davidrgr.proyectoSpring.repositorio.UsuarioRepository;
import es.davidrgr.proyectoSpring.seguridad.LimitadorIntentos;
import es.davidrgr.proyectoSpring.servicio.UsuarioService.ResultadoRegistro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceTest {

	private static final String CORREO = "ana@x.com";

	@Mock
	UsuarioRepository usuarioRepository;
	@Mock
	RolRepository rolRepository;
	@Mock
	PasswordEncoder passwordEncoder;

	// El limitador es real: así se comprueba su efecto de verdad
	private final LimitadorIntentos limitador = new LimitadorIntentos();

	private UsuarioService servicio;

	@BeforeEach
	void preparar() {
		servicio = new UsuarioService(usuarioRepository, rolRepository, passwordEncoder, limitador);
		when(rolRepository.findByNombre("COMPRADOR")).thenReturn(Optional.of(new Rol("COMPRADOR")));
		when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(new Rol("ADMIN")));
		when(passwordEncoder.encode(any())).thenAnswer(i -> "cifrada(" + i.getArgument(0) + ")");
		when(passwordEncoder.matches("Clave1234", "hash")).thenReturn(true);
		when(passwordEncoder.matches("incorrecta", "hash")).thenReturn(false);
	}

	private RegistroForm form(String correo, String password) {
		RegistroForm f = new RegistroForm();
		f.setNombre("Ana");
		f.setCorreo(correo);
		f.setPassword(password);
		f.setConfirmarPassword(password);
		return f;
	}

	private Usuario usuarioExistente(String... roles) {
		Usuario u = new Usuario("Ana", CORREO, "hash");
		for (String rol : roles) {
			u.getRoles().add(new Rol(rol));
		}
		when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(u));
		return u;
	}

	private java.util.List<String> nombresDeRoles(Usuario u) {
		return u.getRoles().stream().map(Rol::getNombre).toList();
	}

	// ---------------- Cuenta nueva ----------------
	@Test
	void unCorreoNuevoCreaLaCuentaConLaContrasenaCifradaYElRol() {
		ResultadoRegistro resultado = servicio.registrarOAsignarRol(form("  Ana@X.com ", "Clave1234"), "COMPRADOR");

		assertThat(resultado).isEqualTo(ResultadoRegistro.CREADO);
		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(captor.capture());
		Usuario guardado = captor.getValue();
		assertThat(guardado.getCorreo()).isEqualTo(CORREO); // sin espacios y en minúsculas
		assertThat(guardado.getPasswordHash()).isEqualTo("cifrada(Clave1234)").isNotEqualTo("Clave1234");
		assertThat(nombresDeRoles(guardado)).containsExactly("COMPRADOR");
	}

	// ---------------- Cuenta existente: añadir un rol ----------------
	@Test
	void unaCuentaExistenteRecibeElRolNuevoSiSeDemuestraLaContrasena() {
		Usuario u = usuarioExistente("COMPRADOR");

		ResultadoRegistro resultado = servicio.registrarOAsignarRol(form(CORREO, "Clave1234"), "ADMIN");

		assertThat(resultado).isEqualTo(ResultadoRegistro.ROL_ANADIDO);
		assertThat(nombresDeRoles(u)).containsExactlyInAnyOrder("COMPRADOR", "ADMIN");
	}

	@Test
	void conUnaContrasenaIncorrectaNoSeAnadeElRol() {
		Usuario u = usuarioExistente("COMPRADOR");

		ResultadoRegistro resultado = servicio.registrarOAsignarRol(form(CORREO, "incorrecta"), "ADMIN");

		assertThat(resultado).isEqualTo(ResultadoRegistro.PASSWORD_INCORRECTA);
		assertThat(nombresDeRoles(u)).containsExactly("COMPRADOR");
		verify(usuarioRepository, never()).save(any());
	}

	@Test
	void siLaCuentaYaTieneElRolSeAvisa() {
		usuarioExistente("COMPRADOR");

		ResultadoRegistro resultado = servicio.registrarOAsignarRol(form(CORREO, "Clave1234"), "COMPRADOR");

		assertThat(resultado).isEqualTo(ResultadoRegistro.YA_TIENE_ROL);
	}

	// ---------------- Límite de intentos ----------------
	@Test
	void tras5ContrasenasIncorrectasLaCuentaSeBloqueaIncluidoConLaContrasenaCorrecta() {
		Usuario u = usuarioExistente("COMPRADOR");

		for (int i = 0; i < 5; i++) {
			assertThat(servicio.registrarOAsignarRol(form(CORREO, "incorrecta"), "ADMIN"))
					.isEqualTo(ResultadoRegistro.PASSWORD_INCORRECTA);
		}

		assertThat(servicio.registrarOAsignarRol(form(CORREO, "Clave1234"), "ADMIN"))
				.isEqualTo(ResultadoRegistro.BLOQUEADO);
		assertThat(nombresDeRoles(u)).containsExactly("COMPRADOR");
	}

	@Test
	void unaContrasenaCorrectaReiniciaElContadorDeFallos() {
		usuarioExistente("COMPRADOR");

		for (int i = 0; i < 4; i++) {
			servicio.registrarOAsignarRol(form(CORREO, "incorrecta"), "ADMIN");
		}
		servicio.registrarOAsignarRol(form(CORREO, "Clave1234"), "COMPRADOR"); // acierta: reinicia

		for (int i = 0; i < 4; i++) {
			assertThat(servicio.registrarOAsignarRol(form(CORREO, "incorrecta"), "ADMIN"))
					.isEqualTo(ResultadoRegistro.PASSWORD_INCORRECTA);
		}
	}
}
