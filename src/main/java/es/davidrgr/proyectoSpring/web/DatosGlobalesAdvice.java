package es.davidrgr.proyectoSpring.web;

import es.davidrgr.proyectoSpring.modelo.Categoria;
import es.davidrgr.proyectoSpring.modelo.EstadoCompra;
import es.davidrgr.proyectoSpring.repositorio.CategoriaRepository;
import es.davidrgr.proyectoSpring.repositorio.DetalleCompraRepository;
import es.davidrgr.proyectoSpring.seguridad.UsuarioAutenticado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Slf4j
@ControllerAdvice
public class DatosGlobalesAdvice {

	//Llamamos al repositorio de las categorias
	private final CategoriaRepository categoriaRepository;
	private final DetalleCompraRepository detalleCompraRepository;

	//Creamos un constructor de la clase y le asignamos la categoriaRepository que declaramos anteriormente
	// (Funciona gracias al AutoWired implicito y lo que tenemos en su interfaz propia)

	public DatosGlobalesAdvice(CategoriaRepository categoriaRepository, DetalleCompraRepository detalleCompraRepository) {
		this.categoriaRepository = categoriaRepository;
		this.detalleCompraRepository = detalleCompraRepository;
	}

	//Metodo para averiguar si el usuario tiene rol, obtiene las autoridades, y un rol y comprueba si el rol existe en las autoridades
	private boolean tieneRol(Authentication auth, String rol) {
		return auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals(rol));
	}

	//Metodo para añadir datos globales en la página
	//Le pasamos el modelo (La pagina web) y el autenticador
	@ModelAttribute
	public void datosGlobales(Model model, Authentication auth) {
		// Comprueba que el usuario esté autenticado
		boolean autenticado = auth != null
				&& auth.isAuthenticated()
				&& !(auth instanceof AnonymousAuthenticationToken);

		// Carga en el modelo las categorías y sus subcategorías (si tienen)
		model.addAttribute("categorias", categoriasSeguras());
		// Carga si el usuario está autenticado
		model.addAttribute("autenticado", autenticado);
		// Carga el nombre del usuario
		String nombre = null;
		if (autenticado) {
			nombre = (auth.getPrincipal() instanceof UsuarioAutenticado u) ? u.getNombre() : auth.getName();
		}
		model.addAttribute("nombreUsuario", nombre);
		// Carga el rol del usuario
		model.addAttribute("esAdmin", autenticado && tieneRol(auth, "ROLE_ADMIN"));
		model.addAttribute("esComprador", autenticado && tieneRol(auth, "ROLE_COMPRADOR"));
		// Unidades del carrito (solo para compradores)
		model.addAttribute("unidadesCarrito",
				autenticado && tieneRol(auth, "ROLE_COMPRADOR") ? unidadesCarritoSeguras(auth.getName()) : 0L);
	}

	private List<Categoria> categoriasSeguras() {
		try {
			return categoriaRepository.findAllByOrderByNombreAsc();
		} catch (RuntimeException e) {
			log.error("No se pudieron cargar las categorías", e);
			return List.of();
		}
	}

	private long unidadesCarritoSeguras(String correo) {
		try {
			return detalleCompraRepository.unidadesEnCarrito(correo, EstadoCompra.PENDIENTE);
		} catch (RuntimeException e) {
			log.error("No se pudo contar el carrito", e);
			return 0L;
		}
	}

}