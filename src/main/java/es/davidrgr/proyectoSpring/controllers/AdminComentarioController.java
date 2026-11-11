package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.dto.ComentarioAdmin;
import es.davidrgr.proyectoSpring.repositorio.ComentarioRepository;
import es.davidrgr.proyectoSpring.servicio.ComentarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.NoSuchElementException;

@Controller
public class AdminComentarioController {

    private static final int TAMANO_PAGINA = 15;

	private final ComentarioRepository comentarioRepository;
	private final ComentarioService comentarioService;

	public AdminComentarioController(ComentarioRepository comentarioRepository, ComentarioService comentarioService) {
		this.comentarioRepository = comentarioRepository;
		this.comentarioService = comentarioService;
	}

	@GetMapping("/admin/comentarios")
	public String listar(@RequestParam(defaultValue = "todos") String estado,
						 @RequestParam(defaultValue = "0") int pagina, Model model) {
		if (!estado.equals("visibles") && !estado.equals("ocultos")) {
			estado = "todos";
		}
		boolean todos = estado.equals("todos");
		boolean visible = estado.equals("visibles");

		Page<ComentarioAdmin> resultado = comentarioRepository.buscarAdmin(todos, visible,
				PageRequest.of(Math.max(pagina, 0), TAMANO_PAGINA,
						Sort.by("fecha").descending().and(Sort.by("idComentario").descending())));

		model.addAttribute("pagina", resultado);
		model.addAttribute("estado", estado);
		model.addAttribute("urlBase", UriComponentsBuilder.fromPath("/admin/comentarios")
				.queryParam("estado", estado).build().toUriString());
		return "admin-comentarios";
	}

	@PostMapping("/admin/comentarios/{id}/visible")
	public String alternarVisible(@PathVariable Long id,
								  @RequestParam(defaultValue = "todos") String estado,
								  @RequestParam(defaultValue = "0") int pagina,
								  RedirectAttributes redirect) {
		boolean visible = comentarioService.alternarVisible(id);
		redirect.addFlashAttribute("aviso", visible ? "Comentario visible de nuevo" : "Comentario oculto");
		redirect.addAttribute("estado", estado);
		redirect.addAttribute("pagina", pagina);
		return "redirect:/admin/comentarios";
	}

	@ExceptionHandler(NoSuchElementException.class)
	public void noEncontrado(HttpServletResponse respuesta) throws IOException {
		respuesta.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
}
