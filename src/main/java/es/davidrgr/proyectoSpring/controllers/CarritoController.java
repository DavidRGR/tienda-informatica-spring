package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.servicio.CarritoException;
import es.davidrgr.proyectoSpring.servicio.CarritoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CarritoController {

	// Importamos el servicio del carrito
    private final CarritoService carritoService;

	public CarritoController(CarritoService carritoService) {
		this.carritoService = carritoService;
	}

	@GetMapping("/carrito")
	public String ver(Authentication auth, Model model){
		model.addAttribute("carrito", carritoService.verCarrito(auth.getName()));
		return "carrito";
	}

	@PostMapping("carrito/agregar/{id}")
	public String agregar(@PathVariable Long id, @RequestParam(defaultValue = "1") int cantidad,
						  Authentication auth, RedirectAttributes redirect) {
		try {
			carritoService.agregar(auth.getName(), id, cantidad);
		}catch (CarritoException e){
			redirect.addFlashAttribute("error", e.getMessage());
			return "redirect:/articulos/" + id;
		}

		redirect.addFlashAttribute("aviso", "Articulo añadido al carrito");
		return "redirect:/carrito";
	}

	@PostMapping("/carrito/cantidad/{idDetalle}")
	public String cambiarCantidad(@PathVariable Long idDetalle, @RequestParam int cantidad,
								  Authentication auth, RedirectAttributes redirect) {
		try {
			carritoService.cambiarCantidad(auth.getName(), idDetalle, cantidad);
		}catch (CarritoException e) {
			redirect.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/carrito";
	}

	@PostMapping("/carrito/quitar/{idDetalle}")
	public String quitar(@PathVariable Long idDetalle, Authentication auth, RedirectAttributes redirect) {
		try {
			carritoService.quitar(auth.getName(), idDetalle);
		}catch (CarritoException e){
			redirect.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/carrito";
	}

	@PostMapping("/carrito/finalizar")
	public String finalizar(Authentication auth, RedirectAttributes redirect) {
		try {
			Long idCompra = carritoService.finalizar(auth.getName());
			return "redirect:/compras/" + idCompra;
		}catch (CarritoException e){
			redirect.addFlashAttribute("error", e.getMessage());
			return "redirect:/carrito";
		}
	}


}
