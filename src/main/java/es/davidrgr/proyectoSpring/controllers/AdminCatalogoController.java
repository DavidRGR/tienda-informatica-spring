package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.servicio.CatalogoAdminService;
import es.davidrgr.proyectoSpring.servicio.CatalogoException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.NoSuchElementException;

@Controller
public class AdminCatalogoController {

	private final CatalogoAdminService servicio;

	public AdminCatalogoController(CatalogoAdminService servicio) {
		this.servicio = servicio;
	}

	@GetMapping("/admin/catalogo")
	public String ver(Model model) {
		model.addAttribute("marcas", servicio.marcas());
		model.addAttribute("categoriasAdmin", servicio.categorias());
		return "admin-catalogo";
	}

	// ---------------- Marcas ----------------
	@PostMapping("/admin/catalogo/marcas")
	public String crearMarca(@RequestParam String nombre, RedirectAttributes redirect) {
		return ejecutar(redirect, "Marca creada", () -> servicio.crearMarca(nombre));
	}

	@PostMapping("/admin/catalogo/marcas/{id}")
	public String renombrarMarca(@PathVariable Long id, @RequestParam String nombre, RedirectAttributes redirect) {
		return ejecutar(redirect, "Marca actualizada", () -> servicio.renombrarMarca(id, nombre));
	}

	@PostMapping("/admin/catalogo/marcas/{id}/eliminar")
	public String eliminarMarca(@PathVariable Long id, RedirectAttributes redirect) {
		return ejecutar(redirect, "Marca eliminada", () -> servicio.eliminarMarca(id));
	}

	// ---------------- Categorías ----------------
	@PostMapping("/admin/catalogo/categorias")
	public String crearCategoria(@RequestParam String nombre, RedirectAttributes redirect) {
		return ejecutar(redirect, "Categoría creada", () -> servicio.crearCategoria(nombre));
	}

	@PostMapping("/admin/catalogo/categorias/{id}")
	public String renombrarCategoria(@PathVariable Long id, @RequestParam String nombre, RedirectAttributes redirect) {
		return ejecutar(redirect, "Categoría actualizada", () -> servicio.renombrarCategoria(id, nombre));
	}

	@PostMapping("/admin/catalogo/categorias/{id}/eliminar")
	public String eliminarCategoria(@PathVariable Long id, RedirectAttributes redirect) {
		return ejecutar(redirect, "Categoría eliminada", () -> servicio.eliminarCategoria(id));
	}

	// ---------------- Subcategorías ----------------
	@PostMapping("/admin/catalogo/categorias/{id}/subcategorias")
	public String crearSubcategoria(@PathVariable Long id, @RequestParam String nombre, RedirectAttributes redirect) {
		return ejecutar(redirect, "Subcategoría creada", () -> servicio.crearSubcategoria(id, nombre));
	}

	@PostMapping("/admin/catalogo/subcategorias/{id}")
	public String renombrarSubcategoria(@PathVariable Long id, @RequestParam String nombre, RedirectAttributes redirect) {
		return ejecutar(redirect, "Subcategoría actualizada", () -> servicio.renombrarSubcategoria(id, nombre));
	}

	@PostMapping("/admin/catalogo/subcategorias/{id}/eliminar")
	public String eliminarSubcategoria(@PathVariable Long id, RedirectAttributes redirect) {
		return ejecutar(redirect, "Subcategoría eliminada", () -> servicio.eliminarSubcategoria(id));
	}

	// ---------------- Utilidades ----------------
	private String ejecutar(RedirectAttributes redirect, String exito, Runnable accion) {
		try {
			accion.run();
			redirect.addFlashAttribute("aviso", exito);
		} catch (CatalogoException e) {
			redirect.addFlashAttribute("error", e.getMessage());
		} catch (DataIntegrityViolationException e) {
			redirect.addFlashAttribute("error", "No se ha podido completar: el nombre ya existe o el elemento está en uso");
		}
		return "redirect:/admin/catalogo";
	}

	@ExceptionHandler(NoSuchElementException.class)
	public void noEncontrado(HttpServletResponse respuesta) throws IOException {
		respuesta.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
}