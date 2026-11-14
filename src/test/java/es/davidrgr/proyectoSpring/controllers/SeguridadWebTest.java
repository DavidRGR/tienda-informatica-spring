package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.dto.CarritoVista;
import es.davidrgr.proyectoSpring.repositorio.*;
import es.davidrgr.proyectoSpring.seguridad.ConfiguracionSeguridad;
import es.davidrgr.proyectoSpring.servicio.ArticuloAdminService;
import es.davidrgr.proyectoSpring.servicio.CarritoService;
import es.davidrgr.proyectoSpring.servicio.ComentarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprueba quién puede entrar en cada zona. La seguridad actúa antes de llegar al controlador,
 * así que en los casos denegados los controladores ni se ejecutan.
 */
@WebMvcTest(controllers = {AdminArticuloController.class, CarritoController.class, ComprasController.class,
		ComentarioController.class, LegalController.class, LoginController.class})
@Import({ConfiguracionSeguridad.class, ConfiguracionPruebasWeb.class})
class SeguridadWebTest {

	@Autowired
	MockMvc mockMvc;

	// Los necesita DatosGlobalesAdvice, que se carga en todas las pruebas web
	@MockitoBean
	CategoriaRepository categoriaRepository;
	@MockitoBean
	DetalleCompraRepository detalleCompraRepository;

	// Los necesitan los controladores
	@MockitoBean
	ArticuloRepository articuloRepository;
	@MockitoBean
	MarcaRepository marcaRepository;
	@MockitoBean
	ArticuloAdminService articuloAdminService;
	@MockitoBean
	CarritoService carritoService;
	@MockitoBean
	CompraRepository compraRepository;
	@MockitoBean
	ComentarioService comentarioService;

	// ---------------- Sin sesión ----------------
	@ParameterizedTest
	@ValueSource(strings = {"/admin/articulos", "/admin/comentarios", "/carrito", "/compras"})
	void sinSesionLasZonasPrivadasLlevanAlLogin(String ruta) throws Exception {
		mockMvc.perform(get(ruta))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void sinSesionNoSePuedeComentar() throws Exception {
		mockMvc.perform(post("/articulos/1/comentarios").with(csrf())
						.param("valoracion", "5").param("texto", "Muy bueno"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"/login", "/legal/cookies"})
	void lasPaginasPublicasSeVenSinSesion(String ruta) throws Exception {
		mockMvc.perform(get(ruta)).andExpect(status().isOk());
	}

	// ---------------- Comprador ----------------
	@Test
	@WithMockUser(roles = "COMPRADOR")
	void unCompradorNoEntraEnAdministracion() throws Exception {
		mockMvc.perform(get("/admin/articulos")).andExpect(status().isForbidden());
		mockMvc.perform(get("/admin/comentarios")).andExpect(status().isForbidden());
		mockMvc.perform(post("/admin/articulos/1/activo").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "COMPRADOR")
	void unCompradorVeSuCarritoYSusCompras() throws Exception {
		when(carritoService.verCarrito(any())).thenReturn(new CarritoVista(List.of()));

		mockMvc.perform(get("/carrito")).andExpect(status().isOk());
		mockMvc.perform(get("/compras")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "COMPRADOR")
	void unPostSinTokenCsrfSeRechaza() throws Exception {
		mockMvc.perform(post("/carrito/finalizar")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "COMPRADOR")
	void unPostConTokenCsrfLlegaAlControlador() throws Exception {
		mockMvc.perform(post("/carrito/finalizar").with(csrf())).andExpect(status().is3xxRedirection());
	}

	// ---------------- Administrador ----------------
	@Test
	@WithMockUser(roles = "ADMIN")
	void unAdminEntraEnElPanelDeArticulos() throws Exception {
		when(articuloRepository.buscarAdmin(any(), any())).thenReturn(Page.empty());

		mockMvc.perform(get("/admin/articulos")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void unAdminSinRolDeCompradorNoEntraEnElCarrito() throws Exception {
		mockMvc.perform(get("/carrito")).andExpect(status().isForbidden());
		mockMvc.perform(get("/compras")).andExpect(status().isForbidden());
		mockMvc.perform(post("/carrito/agregar/1").with(csrf())).andExpect(status().isForbidden());
	}

	// ---------------- Los dos roles en la misma cuenta ----------------
	@Test
	@WithMockUser(roles = {"ADMIN", "COMPRADOR"})
	void unaCuentaConLosDosRolesEntraEnLasDosZonas() throws Exception {
		when(articuloRepository.buscarAdmin(any(), any())).thenReturn(Page.empty());
		when(carritoService.verCarrito(any())).thenReturn(new CarritoVista(List.of()));

		mockMvc.perform(get("/admin/articulos")).andExpect(status().isOk());
		mockMvc.perform(get("/carrito")).andExpect(status().isOk());
	}
}
