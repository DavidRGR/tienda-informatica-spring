package es.davidrgr.proyectoSpring.web;

import es.davidrgr.proyectoSpring.repositorio.CategoriaRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class DatosGlobalesAdvice {

	private final CategoriaRepository categoriaRepository;

	public DatosGlobalesAdvice(CategoriaRepository categoriaRepository) {
		this.categoriaRepository = categoriaRepository;
	}

	private boolean tieneRol(Authentication auth, String rol) {
		return auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals(rol));
	}

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