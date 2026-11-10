package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.dto.ArticuloDetalle;
import es.davidrgr.proyectoSpring.dto.ArticuloTarjeta;
import es.davidrgr.proyectoSpring.dto.ComentarioVista;
import es.davidrgr.proyectoSpring.modelo.Categoria;
import es.davidrgr.proyectoSpring.modelo.EstadoCompra;
import es.davidrgr.proyectoSpring.modelo.Subcategoria;
import es.davidrgr.proyectoSpring.repositorio.ArticuloRepository;
import es.davidrgr.proyectoSpring.repositorio.CategoriaRepository;
import es.davidrgr.proyectoSpring.repositorio.ComentarioRepository;
import es.davidrgr.proyectoSpring.repositorio.SubcategoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class ArticuloController {

	// Constante con el tamaño de la página
	private static final int TAMANO_PAGINA = 12;

	// Inyectamos los repositorios que necesitamos
	private final ArticuloRepository articuloRepository;
	private final CategoriaRepository categoriaRepository;
	private final SubcategoriaRepository subcategoriaRepository;
	private final ComentarioRepository comentarioRepository;

	public ArticuloController(ArticuloRepository articuloRepository, CategoriaRepository categoriaRepository, SubcategoriaRepository subcategoriaRepository, ComentarioRepository comentarioRepository) {
		this.articuloRepository = articuloRepository;
		this.categoriaRepository = categoriaRepository;
		this.subcategoriaRepository = subcategoriaRepository;
		this.comentarioRepository = comentarioRepository;
	}

	// Mapping de la ruta artículo para listarlos
	@GetMapping("/articulos")
	public String listar(@RequestParam(required = false) Long categoria,
						 @RequestParam(required = false) Long subcategoria,
						 @RequestParam(defaultValue = "nombre") String orden,
						 @RequestParam(defaultValue = "0") int pagina,
						 Model model) {

		// Solo se aceptan los criterios de orden conocidos
		Sort sort;
		switch (orden) {
			case "precio-asc" -> sort = Sort.by("precio").ascending();
			case "precio-desc" -> sort = Sort.by("precio").descending();
			case "mejor-valorados" -> sort = JpaSort.unsafe(Sort.Direction.DESC, "coalesce(avg(c.valoracion), 0)")
					.and(JpaSort.unsafe(Sort.Direction.DESC, "count(c)"));
			case "peor-valorados" -> sort = JpaSort.unsafe(Sort.Direction.ASC, "coalesce(avg(c.valoracion), 6)")
					.and(JpaSort.unsafe(Sort.Direction.DESC, "count(c)"));
			default -> { orden = "nombre"; sort = Sort.by("nombre").ascending(); }
		}
		// El id como segundo criterio hace estable la paginación
		sort = sort.and(Sort.by("idArticulo"));

		Pageable pageable = PageRequest.of(Math.max(pagina, 0), TAMANO_PAGINA, sort);

		Page<ArticuloTarjeta> resultado;
		String titulo;

		if (subcategoria != null) {
			resultado = articuloRepository.listarPorSubcategoria(subcategoria, pageable);
			titulo = subcategoriaRepository.findById(subcategoria)
					.map(Subcategoria::getNombre).orElse("Artículos");
		} else if (categoria != null) {
			resultado = articuloRepository.listarPorCategoria(categoria, pageable);
			titulo = categoriaRepository.findById(categoria)
					.map(Categoria::getNombre).orElse("Artículos");
		} else {
			resultado = articuloRepository.listarActivos(pageable);
			titulo = "Todos los artículos";
		}

		// Base de los enlaces de paginación: conserva el filtro y el orden
		UriComponentsBuilder base = UriComponentsBuilder.fromPath("/articulos").queryParam("orden", orden);
		if (subcategoria != null) {
			base.queryParam("subcategoria", subcategoria);
		} else if (categoria != null) {
			base.queryParam("categoria", categoria);
		}

		model.addAttribute("pagina", resultado);
		model.addAttribute("titulo", titulo);
		model.addAttribute("orden", orden);
		model.addAttribute("idCategoria", subcategoria == null ? categoria : null);
		model.addAttribute("idSubcategoria", subcategoria);
		model.addAttribute("urlBase", base.build().toUriString());
		return "articulos";
	}

	@GetMapping("/articulos/{id}")
	public String detalle(@PathVariable Long id,
						  @RequestParam(defaultValue = "recientes") String ordenComentarios,
						  Authentication auth, Model model) {
		ArticuloDetalle articulo = articuloRepository.buscarDetalle(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artículo no encontrado"));

		// La consulta ya los trae del más reciente al más antiguo. La ordenación de List es estable,
		// así que a igual valoración se mantiene el más reciente primero.
		List<ComentarioVista> comentarios = new ArrayList<>(comentarioRepository.listarVisibles(id, EstadoCompra.PAGADA));
		switch (ordenComentarios) {
			case "mejores" -> comentarios.sort(Comparator.comparingInt(ComentarioVista::valoracion).reversed());
			case "peores" -> comentarios.sort(Comparator.comparingInt(ComentarioVista::valoracion));
			default -> ordenComentarios = "recientes";
		}

		model.addAttribute("articulo", articulo);
		model.addAttribute("comentarios", comentarios);
		model.addAttribute("ordenComentarios", ordenComentarios);
		model.addAttribute("valoracion", comentarioRepository.resumen(id));
		model.addAttribute("yaComento", auth != null
				&& comentarioRepository.existsByArticuloIdArticuloAndUsuarioCorreo(id, auth.getName()));
		return "detalle-articulo";
	}

}
