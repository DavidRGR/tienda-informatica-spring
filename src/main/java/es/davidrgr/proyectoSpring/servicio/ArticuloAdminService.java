package es.davidrgr.proyectoSpring.servicio;

import es.davidrgr.proyectoSpring.dto.ArticuloForm;
import es.davidrgr.proyectoSpring.modelo.Articulo;
import es.davidrgr.proyectoSpring.repositorio.ArticuloRepository;
import es.davidrgr.proyectoSpring.repositorio.CategoriaRepository;
import es.davidrgr.proyectoSpring.repositorio.MarcaRepository;
import es.davidrgr.proyectoSpring.repositorio.SubcategoriaRepository;
import es.davidrgr.proyectoSpring.util.Imagenes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Service
public class ArticuloAdminService {

    //Inyectamos los repositorios
	private final ArticuloRepository articuloRepository;
	private final MarcaRepository marcaRepository;
	private final CategoriaRepository categoriaRepository;
	private final SubcategoriaRepository subcategoriaRepository;
	private final AlmacenImagenes almacen;

	public ArticuloAdminService(ArticuloRepository articuloRepository, MarcaRepository marcaRepository, CategoriaRepository categoriaRepository, SubcategoriaRepository subcategoriaRepository, AlmacenImagenes almacen) {
		this.articuloRepository = articuloRepository;
		this.marcaRepository = marcaRepository;
		this.categoriaRepository = categoriaRepository;
		this.subcategoriaRepository = subcategoriaRepository;
		this.almacen = almacen;
	}

	private boolean hayImagen(ArticuloForm f) {
		return f.getImagen() != null && !f.getImagen().isEmpty();
	}

	private void aplicar(Articulo a, ArticuloForm f) {
		a.setNombre(f.getNombre().trim());
		a.setDescripcion(f.getDescripcion() == null || f.getDescripcion().isBlank() ? null : f.getDescripcion().trim());
		a.setPrecio(f.getPrecio());
		a.setStock(f.getStock());
		a.setActivo(f.isActivo());
		a.setMarca(marcaRepository.findById(f.getIdMarca()).orElseThrow());
		a.setCategoria(categoriaRepository.findById(f.getIdCategoria()).orElseThrow());
		a.setSubcategoria(f.getIdSubcategoria() == null ? null
				: subcategoriaRepository.findById(f.getIdSubcategoria()).orElseThrow());
	}

	@Transactional(readOnly = true)
	public boolean subcategoriaEsDe(Long idSubcategoria, Long idCategoria) {
		return subcategoriaRepository.existsByIdSubcategoriaAndCategoriaIdCategoria(idSubcategoria, idCategoria);
	}

	@Transactional(readOnly = true)
	public Optional<ArticuloForm> formularioDe(Long id) {
		return articuloRepository.findById(id).map(a -> {
			ArticuloForm f = new ArticuloForm();
			f.setNombre(a.getNombre());
			f.setDescripcion(a.getDescripcion());
			f.setPrecio(a.getPrecio());
			f.setStock(a.getStock());
			f.setActivo(a.isActivo());
			f.setIdMarca(a.getMarca().getIdMarca());
			f.setIdCategoria(a.getCategoria().getIdCategoria());
			f.setIdSubcategoria(a.getSubcategoria() == null ? null : a.getSubcategoria().getIdSubcategoria());
			return f;
		});
	}

	@Transactional(readOnly = true)
	public String srcImagen(Long id) {
		return articuloRepository.findById(id)
				.map(Articulo::getImagenUrl)
				.filter(u -> u != null && !u.isBlank())
				.map(Imagenes::resolver)
				.orElse(null);
	}

	@Transactional
	public void crear(ArticuloForm f) {
		String nueva = hayImagen(f) ? almacen.guardar(f.getImagen()) : null;
		try {
			Articulo a = new Articulo();
			aplicar(a, f);
			a.setImagenUrl(nueva);
			articuloRepository.saveAndFlush(a);
		}catch (RuntimeException e) {
			almacen.borrar(nueva);
			throw e;
		}
	}

	@Transactional
	public void actualizar(Long id, ArticuloForm f) {
		Articulo a = articuloRepository.findById(id).orElseThrow();
		String anterior = a.getImagenUrl();
		String nueva = hayImagen(f) ? almacen.guardar(f.getImagen()) : null;
		try {
			aplicar(a, f);
			if (nueva != null) {
				a.setImagenUrl(nueva);
			}
			articuloRepository.saveAndFlush(a);
		}catch (RuntimeException e) {
			almacen.borrar(nueva);
			throw e;
		}
		if (nueva != null) {
			// La foto antigua se borra solo si la transacción termina bien
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					almacen.borrar(anterior);
				}
			});
		}
	}

	@Transactional
	public boolean alternarActivo(Long id) {
		Articulo a = articuloRepository.findById(id).orElseThrow();
		a.setActivo(!a.isActivo());
		return a.isActivo();
	}
}
