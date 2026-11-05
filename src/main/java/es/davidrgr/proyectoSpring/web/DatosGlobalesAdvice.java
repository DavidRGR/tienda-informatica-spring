package es.davidrgr.proyectoSpring.web;

import es.davidrgr.proyectoSpring.repositorio.CategoriaRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class DatosGlobalesAdvice {

	//Llamamos al repositorio de las categorias
	private final CategoriaRepository categoriaRepository;

	//Creamos un constructor de la clase y le asignamos la categoriaRepository que declaramos anteriormente
	// (Funciona gracias al AutoWired implicito y lo que tenemos en su interfz propia)
	public DatosGlobalesAdvice(CategoriaRepository categoriaRepository) {
		this.categoriaRepository = categoriaRepository;
	}

	//Metodo para averiguar si el usuario tiene rol, obtiene las autoridades, y un rol y comprueba si el rol existe en las autoridades
	private boolean tieneRol(Authentication auth, String rol) {
		return auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals(rol));
	}
	//Metodo para añadir datos globales en la página
	//Le pasamos el modelo (La pagina web) y el autenticador
	//Comprueba que usuario este autenticado
	//Carga en el modelo las categorias y sus consiguientes subcategorias (Si tienen)
	//Carga el nombre del usuario
	//Carga el rol del usuario
	@ModelAttribute
	public void datosGlobales(Model model, Authentication auth) {
		boolean autenticado = auth != null
				&& auth.isAuthenticated()
				&& !(auth instanceof AnonymousAuthenticationToken);

		model.addAttribute("categorias", categoriaRepository.findAllByOrderByNombreAsc());
		model.addAttribute("autenticado", autenticado);
		model.addAttribute("nombreUsuario", autenticado ? auth.getName() : null);
		model.addAttribute("esAdmin", autenticado && tieneRol(auth, "ROLE_ADMIN"));
		model.addAttribute("esComprador", autenticado && tieneRol(auth, "ROLE_COMPRADOR"));
	}
}