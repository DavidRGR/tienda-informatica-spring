package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.modelo.Auditoria;
import es.davidrgr.proyectoSpring.repositorio.AuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class AdminAuditoriaController {

    private static final int TAMANO_PAGINA = 20;

	private final AuditoriaRepository auditoriaRepository;

	public AdminAuditoriaController(AuditoriaRepository auditoriaRepository) {
		this.auditoriaRepository = auditoriaRepository;
	}

	@GetMapping("/admin/auditoria")
	public String listar(@RequestParam(defaultValue = "") String q,
						 @RequestParam(defaultValue = "0") int pagina, Model model) {
		String busqueda = q.trim();
		Page<Auditoria> resultado = auditoriaRepository.buscar(busqueda,
				PageRequest.of(Math.max(pagina, 0), TAMANO_PAGINA,
						Sort.by("fecha").descending().and(Sort.by("idAuditoria").descending())));

		model.addAttribute("pagina", resultado);
		model.addAttribute("q", busqueda);
		model.addAttribute("urlBase", UriComponentsBuilder.fromPath("/admin/auditoria")
				.queryParam("q", busqueda).build().encode().toUriString());
		return "admin-auditoria";
	}
}
