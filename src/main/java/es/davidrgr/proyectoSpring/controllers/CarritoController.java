package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.servicio.CarritoException;
import es.davidrgr.proyectoSpring.servicio.CarritoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CarritoController {

	private static final String CABECERA = "X-Requested-With";

	private final CarritoService carritoService;

	public CarritoController(CarritoService carritoService) {
		this.carritoService = carritoService;
	}

	// Página completa del carrito
	@GetMapping("/carrito")
	public String ver(Authentication auth, Model model) {
		model.addAttribute("carrito", carritoService.verCarrito(auth.getName()));
		return "carrito";
	}

	// Contenido del panel lateral
	@GetMapping("/carrito/panel")
	public String panel(Authentication auth, Model model) {
		model.addAttribute("carrito", carritoService.verCarrito(auth.getName()));
		return "fragments/carrito-panel :: panel";
	}

	@PostMapping("/carrito/agregar/{id}")
	public String agregar(@PathVariable Long id, @RequestParam(defaultValue = "1") int cantidad,
						  @RequestHeader(value = CABECERA, required = false) String cabecera,
						  Authentication auth, Model model, RedirectAttributes redirect) {
		try {
			carritoService.agregar(auth.getName(), id, cantidad);
		} catch (CarritoException e) {
			return responder(cabecera, "/articulos/" + id, e.getMessage(), null, auth, model, redirect);
		}
		return responder(cabecera, "/carrito", null, "Artículo añadido al carrito", auth, model, redirect);
	}

	@PostMapping("/carrito/cantidad/{idDetalle}")
	public String cambiarCantidad(@PathVariable Long idDetalle, @RequestParam int cantidad,
								  @RequestHeader(value = CABECERA, required = false) String cabecera,
								  Authentication auth, Model model, RedirectAttributes redirect) {
		String error = null;
		try {
			carritoService.cambiarCantidad(auth.getName(), idDetalle, cantidad);
		} catch (CarritoException e) {
			error = e.getMessage();
		}
		return responder(cabecera, "/carrito", error, null, auth, model, redirect);
	}

	@PostMapping("/carrito/quitar/{idDetalle}")
	public String quitar(@PathVariable Long idDetalle,
						 @RequestHeader(value = CABECERA, required = false) String cabecera,
						 Authentication auth, Model model, RedirectAttributes redirect) {
		String error = null;
		try {
			carritoService.quitar(auth.getName(), idDetalle);
		} catch (CarritoException e) {
			error = e.getMessage();
		}
		return responder(cabecera, "/carrito", error, null, auth, model, redirect);
	}

	@PostMapping("/carrito/finalizar")
	public String finalizar(Authentication auth, RedirectAttributes redirect) {
		try {
			Long idCompra = carritoService.finalizar(auth.getName());
			return "redirect:/compras/" + idCompra;
		} catch (CarritoException e) {
			redirect.addFlashAttribute("error", e.getMessage());
			return "redirect:/carrito";
		}
	}

	// Con la cabecera de fetch devuelve el panel; sin ella, redirige como siempre
	private String responder(String cabecera, String destino, String error, String aviso,
							 Authentication auth, Model model, RedirectAttributes redirect) {
		if ("fetch".equals(cabecera)) {
			model.addAttribute("carrito", carritoService.verCarrito(auth.getName()));
			model.addAttribute("errorPanel", error);
			model.addAttribute("avisoPanel", aviso);
			return "fragments/carrito-panel :: panel";
		}
		if (error != null) {
			redirect.addFlashAttribute("error", error);
		}
		if (aviso != null) {
			redirect.addFlashAttribute("aviso", aviso);
		}
		return "redirect:" + destino;
	}
}