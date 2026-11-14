package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.dto.RegistroForm;
import es.davidrgr.proyectoSpring.repositorio.CategoriaRepository;
import es.davidrgr.proyectoSpring.repositorio.DetalleCompraRepository;
import es.davidrgr.proyectoSpring.seguridad.ConfiguracionSeguridad;
import es.davidrgr.proyectoSpring.servicio.UsuarioService;
import es.davidrgr.proyectoSpring.servicio.UsuarioService.ResultadoRegistro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RegistroController.class, properties = "app.admin-code=secreto")
@Import({ConfiguracionSeguridad.class, ConfiguracionPruebasWeb.class})
class RegistroControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	UsuarioService usuarioService;

	// Los necesita DatosGlobalesAdvice
	@MockitoBean
	CategoriaRepository categoriaRepository;
	@MockitoBean
	DetalleCompraRepository detalleCompraRepository;

	private MockHttpServletRequestBuilder registro(String ruta, String password, String confirmar) {
		return post(ruta).with(csrf())
				.param("nombre", "Ana")
				.param("correo", "ana@x.com")
				.param("password", password)
				.param("confirmarPassword", confirmar);
	}

	// ---------------- Compradores ----------------
	@Test
	void unRegistroValidoCreaLaCuentaDeCompradorYLlevaAlLogin() throws Exception {
		when(usuarioService.registrarOAsignarRol(any(RegistroForm.class), eq("COMPRADOR")))
				.thenReturn(ResultadoRegistro.CREADO);

		mockMvc.perform(registro("/registro", "Clave1234", "Clave1234"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?registrado"));

		verify(usuarioService).registrarOAsignarRol(any(RegistroForm.class), eq("COMPRADOR"));
	}

	@Test
	void lasContrasenasDistintasSeRechazan() throws Exception {
		mockMvc.perform(registro("/registro", "Clave1234", "Otra12345"))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrors("registroForm", "confirmarPassword"));

		verify(usuarioService, never()).registrarOAsignarRol(any(), any());
	}

	@Test
	void unaContrasenaDeMenosDeOchoCaracteresSeRechaza() throws Exception {
		mockMvc.perform(registro("/registro", "corta", "corta"))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrors("registroForm", "password"));

		verify(usuarioService, never()).registrarOAsignarRol(any(), any());
	}

	@Test
	void unCorreoExistenteConContrasenaIncorrectaMuestraElErrorEnElCampo() throws Exception {
		when(usuarioService.registrarOAsignarRol(any(RegistroForm.class), eq("COMPRADOR")))
				.thenReturn(ResultadoRegistro.PASSWORD_INCORRECTA);

		mockMvc.perform(registro("/registro", "Clave1234", "Clave1234"))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrors("registroForm", "password"));
	}

	@Test
	void unaCuentaBloqueadaMuestraElErrorEnElCampo() throws Exception {
		when(usuarioService.registrarOAsignarRol(any(RegistroForm.class), eq("COMPRADOR")))
				.thenReturn(ResultadoRegistro.BLOQUEADO);

		mockMvc.perform(registro("/registro", "Clave1234", "Clave1234"))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrors("registroForm", "password"));
	}

	@Test
	void sinTokenCsrfElRegistroSeRechaza() throws Exception {
		mockMvc.perform(post("/registro")
						.param("nombre", "Ana").param("correo", "ana@x.com")
						.param("password", "Clave1234").param("confirmarPassword", "Clave1234"))
				.andExpect(status().isForbidden());
	}

	// ---------------- Empleados ----------------
	@Test
	void conUnCodigoDeEmpleadosIncorrectoNoSeCreaLaCuenta() throws Exception {
		mockMvc.perform(registro("/registro-admin", "Clave1234", "Clave1234").param("codigoAdmin", "mal"))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrors("registroForm", "codigoAdmin"));

		verify(usuarioService, never()).registrarOAsignarRol(any(), any());
	}

	@Test
	void sinCodigoDeEmpleadosNoSeCreaLaCuenta() throws Exception {
		mockMvc.perform(registro("/registro-admin", "Clave1234", "Clave1234"))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrors("registroForm", "codigoAdmin"));

		verify(usuarioService, never()).registrarOAsignarRol(any(), any());
	}

	@Test
	void conElCodigoCorrectoSeCreaLaCuentaDeAdministrador() throws Exception {
		when(usuarioService.registrarOAsignarRol(any(RegistroForm.class), eq("ADMIN")))
				.thenReturn(ResultadoRegistro.CREADO);

		mockMvc.perform(registro("/registro-admin", "Clave1234", "Clave1234").param("codigoAdmin", "secreto"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?registrado"));

		verify(usuarioService).registrarOAsignarRol(any(RegistroForm.class), eq("ADMIN"));
	}
}
