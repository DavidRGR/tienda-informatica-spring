package es.davidrgr.proyectoSpring.servicio;

import es.davidrgr.proyectoSpring.dto.CategoriaAdmin;
import es.davidrgr.proyectoSpring.dto.MarcaAdmin;
import es.davidrgr.proyectoSpring.dto.SubcategoriaAdmin;
import es.davidrgr.proyectoSpring.modelo.Categoria;
import es.davidrgr.proyectoSpring.modelo.Marca;
import es.davidrgr.proyectoSpring.modelo.Subcategoria;
import es.davidrgr.proyectoSpring.repositorio.ArticuloRepository;
import es.davidrgr.proyectoSpring.repositorio.CategoriaRepository;
import es.davidrgr.proyectoSpring.repositorio.MarcaRepository;
import es.davidrgr.proyectoSpring.repositorio.SubcategoriaRepository;
import es.davidrgr.proyectoSpring.seguridad.Auditable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CatalogoAdminService {

	private final MarcaRepository marcaRepository;
	private final CategoriaRepository categoriaRepository;
	private final SubcategoriaRepository subcategoriaRepository;
	private final ArticuloRepository articuloRepository;

	public CatalogoAdminService(MarcaRepository marcaRepository, CategoriaRepository categoriaRepository, SubcategoriaRepository subcategoriaRepository, ArticuloRepository articuloRepository) {
		this.marcaRepository = marcaRepository;
		this.categoriaRepository = categoriaRepository;
		this.subcategoriaRepository = subcategoriaRepository;
		this.articuloRepository = articuloRepository;
	}

	// ---------------- Utilidades ----------------
	private String limpiar(String nombre) {
		String n = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
		if (n.isEmpty()) {
			throw new CatalogoException("El nombre no puede estar vacío");
		}
		if (n.length() > 100) {
			throw new CatalogoException("El nombre no puede superar los 100 caracteres");
		}
		return n;
	}

	@Transactional(readOnly = true)
	public List<MarcaAdmin> marcas() {
		return marcaRepository.listarAdmin();
	}

	@Transactional(readOnly = true)
	public List<CategoriaAdmin> categorias() {
		Map<Long, List<SubcategoriaAdmin>> porCategoria = subcategoriaRepository.listarAdmin().stream()
				.collect(Collectors.groupingBy(SubcategoriaAdmin::idCategoria));
		return categoriaRepository.listarAdmin().stream()
				.map(c -> new CategoriaAdmin(c.id(), c.nombre(), c.articulos(),
						porCategoria.getOrDefault(c.id(), List.of())))
				.toList();
	}

	// ---------------- Marcas ----------------
	@Auditable(accion = "CREAR", entidad = "Marca")
	@Transactional
	public Long crearMarca(String nombre) {
		String n = limpiar(nombre);
		if (marcaRepository.existsByNombreIgnoreCase(n)) {
			throw new CatalogoException("Ya existe la marca «" + n + "»");
		}
		return marcaRepository.saveAndFlush(new Marca(n)).getIdMarca();
	}

	@Auditable(accion = "EDITAR", entidad = "Marca")
	@Transactional
	public void renombrarMarca(Long id, String nombre) {
		Marca marca = marcaRepository.findById(id).orElseThrow();
		String n = limpiar(nombre);
		if (marcaRepository.existsByNombreIgnoreCaseAndIdMarcaNot(n, id)) {
			throw new CatalogoException("Ya existe la marca «" + n + "»");
		}
		marca.setNombre(n);
	}

	@Auditable(accion = "ELIMINAR", entidad = "Marca")
	@Transactional
	public void eliminarMarca(Long id) {
		Marca marca = marcaRepository.findById(id).orElseThrow();
		long usos = articuloRepository.countByMarcaIdMarca(id);
		if (usos > 0) {
			throw new CatalogoException("No se puede eliminar «" + marca.getNombre() + "»: la usan " + usos + " artículos");
		}
		marcaRepository.delete(marca);
	}

	// ---------------- Categorías ----------------
	@Auditable(accion = "CREAR", entidad = "Categoria")
	@Transactional
	public Long crearCategoria(String nombre) {
		String n = limpiar(nombre);
		if (categoriaRepository.existsByNombreIgnoreCase(n)) {
			throw new CatalogoException("Ya existe la categoría «" + n + "»");
		}
		return categoriaRepository.saveAndFlush(new Categoria(n)).getIdCategoria();
	}

	@Auditable(accion = "EDITAR", entidad = "Categoria")
	@Transactional
	public void renombrarCategoria(Long id, String nombre) {
		Categoria categoria = categoriaRepository.findById(id).orElseThrow();
		String n = limpiar(nombre);
		if (categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNot(n, id)) {
			throw new CatalogoException("Ya existe la categoría «" + n + "»");
		}
		categoria.setNombre(n);
	}

	@Auditable(accion = "ELIMINAR", entidad = "Categoria")
	@Transactional
	public void eliminarCategoria(Long id) {
		Categoria categoria = categoriaRepository.findById(id).orElseThrow();
		long usos = articuloRepository.countByCategoriaIdCategoria(id);
		if (usos > 0) {
			throw new CatalogoException("No se puede eliminar «" + categoria.getNombre() + "»: la usan " + usos + " artículos");
		}
		if (subcategoriaRepository.countByCategoriaIdCategoria(id) > 0) {
			throw new CatalogoException("Elimina primero las subcategorías de «" + categoria.getNombre() + "»");
		}
		categoriaRepository.delete(categoria);
	}

	// ---------------- Subcategorías ----------------
	@Auditable(accion = "CREAR", entidad = "Subcategoria")
	@Transactional
	public Long crearSubcategoria(Long idCategoria, String nombre) {
		Categoria categoria = categoriaRepository.findById(idCategoria).orElseThrow();
		String n = limpiar(nombre);
		if (subcategoriaRepository.existsByCategoriaIdCategoriaAndNombreIgnoreCase(idCategoria, n)) {
			throw new CatalogoException("«" + categoria.getNombre() + "» ya tiene la subcategoría «" + n + "»");
		}
		return subcategoriaRepository.saveAndFlush(new Subcategoria(n, categoria)).getIdSubcategoria();
	}

	@Auditable(accion = "EDITAR", entidad = "Subcategoria")
	@Transactional
	public void renombrarSubcategoria(Long id, String nombre) {
		Subcategoria sub = subcategoriaRepository.findById(id).orElseThrow();
		String n = limpiar(nombre);
		if (subcategoriaRepository.existsByCategoriaIdCategoriaAndNombreIgnoreCaseAndIdSubcategoriaNot(
				sub.getCategoria().getIdCategoria(), n, id)) {
			throw new CatalogoException("Ya existe la subcategoría «" + n + "» en esa categoría");
		}
		sub.setNombre(n);
	}

	@Auditable(accion = "ELIMINAR", entidad = "Subcategoria")
	@Transactional
	public void eliminarSubcategoria(Long id) {
		Subcategoria sub = subcategoriaRepository.findById(id).orElseThrow();
		long usos = articuloRepository.countBySubcategoriaIdSubcategoria(id);
		if (usos > 0) {
			throw new CatalogoException("No se puede eliminar «" + sub.getNombre() + "»: la usan " + usos + " artículos");
		}
		subcategoriaRepository.delete(sub);
	}
}
