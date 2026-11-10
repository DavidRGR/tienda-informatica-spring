package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.dto.ArticuloAdmin;
import es.davidrgr.proyectoSpring.repositorio.ArticuloRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class AdminArticuloController {

    private static final int TAMANO_PAGINA = 15;

	private final ArticuloRepository articuloRepository;

	public AdminArticuloController(ArticuloRepository articuloRepository) {
		this.articuloRepository = articuloRepository;
	}

	@GetMapping("/admin")
	public String inicio() {
		return "redirect:/admin/articulos";
	}

	@GetMapping("/admin/articulos")
	public String listar(@RequestParam(defaultValue = "") String q,
						 @RequestParam(defaultValue = "0") int pagina, Model model) {
		String busqueda = q.trim();
		Page<ArticuloAdmin> resultado = articuloRepository.buscarAdmin(busqueda,
				PageRequest.of(Math.max(pagina, 0), TAMANO_PAGINA, Sort.by("idArticulo").descending()));

		model.addAttribute("pagina", resultado);
		model.addAttribute("q", busqueda);
		model.addAttribute("urlBase", UriComponentsBuilder.fromPath("/admin/articulos")
				.queryParam("q", busqueda).build().encode().toUriString());
		return "admin-articulos";
	}
}
