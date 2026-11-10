package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.servicio.ComentarioException;
import es.davidrgr.proyectoSpring.servicio.ComentarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ComentarioController {

    //Inyectamos el service de comentario
	private final ComentarioService comentarioService;

	public ComentarioController(ComentarioService comentarioService) {
		this.comentarioService = comentarioService;
	}

	@PostMapping("/articulos/{id}/comentarios")
	public String comentar(@PathVariable Long id, @RequestParam int valoracion, @RequestParam String texto, Authentication auth, RedirectAttributes redirect) {
		try {
			comentarioService.crear(auth.getName(), id, valoracion, texto);
			redirect.addFlashAttribute("avisoComentario", "¡Gracias por su valoración!");
		}catch (ComentarioException e) {
			redirect.addFlashAttribute("errorComentario", e.getMessage());
		}
		return "redirect:/articulos/" + id +"#comentarios";
	}
}
