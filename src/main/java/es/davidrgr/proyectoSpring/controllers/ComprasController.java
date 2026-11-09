package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.modelo.Compra;
import es.davidrgr.proyectoSpring.modelo.EstadoCompra;
import es.davidrgr.proyectoSpring.repositorio.CompraRepository;
import es.davidrgr.proyectoSpring.repositorio.DetalleCompraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ComprasController {

	//Inyectamos repository
	private final CompraRepository compraRepository;
	private final DetalleCompraRepository detalleRepository;

	public ComprasController(CompraRepository compraRepository, DetalleCompraRepository detalleRepository) {
		this.compraRepository = compraRepository;
		this.detalleRepository = detalleRepository;
	}

	@GetMapping("/compras")
	public String listar(Authentication auth, Model model) {
		model.addAttribute("compras", compraRepository.resumenes(auth.getName(), EstadoCompra.PENDIENTE));
		return "mis-compras";
	}

	@GetMapping("/compras/{id}")
	public String ver(@PathVariable Long id, Authentication auth, Model model) {
		// Solo se puede ver una compra propia
		Compra compra = compraRepository.findByIdCompraAndUsuarioCorreo(id, auth.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
		model.addAttribute("compra", compra);
		model.addAttribute("lineas", detalleRepository.lineasDeCompra(id, auth.getName()));
		return "compra";
	}
}
