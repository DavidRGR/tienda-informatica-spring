package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.dto.ArticuloAdmin;
import es.davidrgr.proyectoSpring.dto.ArticuloForm;
import es.davidrgr.proyectoSpring.repositorio.ArticuloRepository;
import es.davidrgr.proyectoSpring.repositorio.MarcaRepository;
import es.davidrgr.proyectoSpring.servicio.AlmacenImagenes;
import es.davidrgr.proyectoSpring.servicio.ArticuloAdminService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.NoSuchElementException;

@Controller
public class AdminArticuloController {

	private static final int TAMANO_PAGINA = 15;

	private final ArticuloRepository articuloRepository;
	private final MarcaRepository marcaRepository;
	private final ArticuloAdminService servicio;
	private final AlmacenImagenes almacen;

	public AdminArticuloController(ArticuloRepository articuloRepository, MarcaRepository marcaRepository,
								   ArticuloAdminService servicio, AlmacenImagenes almacen) {
		this.articuloRepository = articuloRepository;
		this.marcaRepository = marcaRepository;
		this.servicio = servicio;
		this.almacen = almacen;
	}

	@GetMapping("/admin")
	public String inicio() {
		return "redirect:/admin/articulos";
	}

	// ---------------- Listado ----------------
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

	// ---------------- Alta ----------------
	@GetMapping("/admin/articulos/nuevo")
	public String nuevo(Model model) {
		model.addAttribute("articuloForm", new ArticuloForm());
		prepararFormulario(model, null);
		return "admin-articulo-form";
	}

	@PostMapping("/admin/articulos")
	public String crear(@Valid @ModelAttribute("articuloForm") ArticuloForm form, BindingResult result,
						Model model, RedirectAttributes redirect) {
		validarExtra(form, result);
		if (result.hasErrors()) {
			prepararFormulario(model, null);
			return "admin-articulo-form";
		}
		servicio.crear(form);
		redirect.addFlashAttribute("aviso", "Artículo creado correctamente");
		return "redirect:/admin/articulos";
	}

	// ---------------- Edición ----------------
	@GetMapping("/admin/articulos/{id}/editar")
	public String editar(@PathVariable Long id, Model model) {
		ArticuloForm form = servicio.formularioDe(id).orElseThrow();
		model.addAttribute("articuloForm", form);
		prepararFormulario(model, id);
		return "admin-articulo-form";
	}

	@PostMapping("/admin/articulos/{id}")
	public String actualizar(@PathVariable Long id,
							 @Valid @ModelAttribute("articuloForm") ArticuloForm form, BindingResult result,
							 Model model, RedirectAttributes redirect) {
		validarExtra(form, result);
		if (result.hasErrors()) {
			prepararFormulario(model, id);
			return "admin-articulo-form";
		}
		servicio.actualizar(id, form);
		redirect.addFlashAttribute("aviso", "Artículo actualizado correctamente");
		return "redirect:/admin/articulos";
	}

	// ---------------- Activar / desactivar ----------------
	@PostMapping("/admin/articulos/{id}/activo")
	public String alternarActivo(@PathVariable Long id, RedirectAttributes redirect) {
		boolean activo = servicio.alternarActivo(id);
		redirect.addFlashAttribute("aviso", activo ? "Artículo activado" : "Artículo desactivado");
		return "redirect:/admin/articulos";
	}

	// ---------------- Utilidades ----------------
	private void prepararFormulario(Model model, Long idArticulo) {
		model.addAttribute("marcas", marcaRepository.findAllByOrderByNombreAsc());
		model.addAttribute("idArticulo", idArticulo);
		model.addAttribute("imagenActualSrc", idArticulo == null ? null : servicio.srcImagen(idArticulo));
	}

	private void validarExtra(ArticuloForm form, BindingResult result) {
		if (!result.hasFieldErrors("idCategoria") && form.getIdSubcategoria() != null
				&& !servicio.subcategoriaEsDe(form.getIdSubcategoria(), form.getIdCategoria())) {
			result.rejectValue("idSubcategoria", "subInvalida", "La subcategoría no pertenece a la categoría elegida");
		}
		String errorImagen = almacen.validar(form.getImagen());
		if (errorImagen != null) {
			result.rejectValue("imagen", "imagenInvalida", errorImagen);
		}
	}

	@ExceptionHandler(NoSuchElementException.class)
	public void noEncontrado(HttpServletResponse respuesta) throws IOException {
		respuesta.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
}