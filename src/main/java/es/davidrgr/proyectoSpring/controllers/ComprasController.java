package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.modelo.Compra;
import es.davidrgr.proyectoSpring.modelo.EstadoCompra;
import es.davidrgr.proyectoSpring.repositorio.CompraRepository;
import es.davidrgr.proyectoSpring.repositorio.DetalleCompraRepository;
import es.davidrgr.proyectoSpring.servicio.CarritoException;
import es.davidrgr.proyectoSpring.servicio.CarritoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.NoSuchElementException;

@Controller
public class ComprasController {

	private final CompraRepository compraRepository;
	private final DetalleCompraRepository detalleRepository;
	private final CarritoService carritoService;

	public ComprasController(CompraRepository compraRepository, DetalleCompraRepository detalleRepository,
							 CarritoService carritoService) {
		this.compraRepository = compraRepository;
		this.detalleRepository = detalleRepository;
		this.carritoService = carritoService;
	}

	@GetMapping("/compras")
	public String listar(Authentication auth, Model model) {
		model.addAttribute("compras", compraRepository.resumenes(auth.getName(), EstadoCompra.PENDIENTE));
		return "mis-compras";
	}

	@GetMapping("/compras/{id}")
	public String ver(@PathVariable Long id, Authentication auth, Model model) {
		// Solo se puede ver una compra propia
		Compra compra = compraRepository.findByIdCompraAndUsuarioCorreo(id, auth.getName()).orElseThrow();
		model.addAttribute("compra", compra);
		model.addAttribute("lineas", detalleRepository.lineasDeCompra(id, auth.getName()));
		return "compra";
	}

	@PostMapping("/compras/{id}/cancelar")
	public String cancelar(@PathVariable Long id, @RequestParam(defaultValue = "detalle") String origen,
						   Authentication auth, RedirectAttributes redirect) {
		try {
			carritoService.cancelar(auth.getName(), id);
			redirect.addFlashAttribute("aviso", "Compra cancelada. Los artículos han vuelto al stock.");
		} catch (CarritoException e) {
			redirect.addFlashAttribute("error", e.getMessage());
		}
		return origen.equals("lista") ? "redirect:/compras" : "redirect:/compras/" + id;
	}

	@ExceptionHandler(NoSuchElementException.class)
	public void noEncontrado(HttpServletResponse respuesta) throws IOException {
		respuesta.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
}