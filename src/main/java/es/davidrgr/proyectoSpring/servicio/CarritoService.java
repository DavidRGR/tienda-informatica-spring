package es.davidrgr.proyectoSpring.servicio;

import es.davidrgr.proyectoSpring.dto.CarritoVista;
import es.davidrgr.proyectoSpring.modelo.*;
import es.davidrgr.proyectoSpring.repositorio.ArticuloRepository;
import es.davidrgr.proyectoSpring.repositorio.CompraRepository;
import es.davidrgr.proyectoSpring.repositorio.DetalleCompraRepository;
import es.davidrgr.proyectoSpring.repositorio.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CarritoService {

    //Importamos todas los repository que necesitamos
	private final CompraRepository compraRepository;
	private final DetalleCompraRepository detalleRepository;
	private final ArticuloRepository articuloRepository;
	private final UsuarioRepository usuarioRepository;

	public CarritoService(CompraRepository compraRepository, DetalleCompraRepository detalleRepository, ArticuloRepository articuloRepository, UsuarioRepository usuarioRepository) {
		this.compraRepository = compraRepository;
		this.detalleRepository = detalleRepository;
		this.articuloRepository = articuloRepository;
		this.usuarioRepository = usuarioRepository;
	}

	// ----------------- utilidades -----------------
	private Compra obtenerOCrearCarrito(String correo) {
		return compraRepository.findFirstByUsuarioCorreoAndEstadoOrderByIdCompraAsc(correo, EstadoCompra.PENDIENTE)
				.orElseGet(() -> {
					Usuario usuario = usuarioRepository.findByCorreo(correo)
							.orElseThrow(() -> new CarritoException("Usuario no encontrado"));
					return compraRepository.save(new Compra(usuario));
				});
	}

	private DetalleCompra lineaDelCarrito(String correo, Long idDetalle) {
		return detalleRepository
				.findByIdDetalleAndCompraUsuarioCorreoAndCompraEstado(idDetalle, correo, EstadoCompra.PENDIENTE)
				.orElseThrow(() -> new CarritoException("Esa línea no está en tu carrito"));
	}

	private void comprobarStock(Articulo articulo, int cantidad) {
		if (cantidad > articulo.getStock()) {
			throw new CarritoException("Solo quedan " + articulo.getStock()
					+ " unidades de «" + articulo.getNombre() + "»");
		}
	}

	@Transactional(readOnly = true)
	public CarritoVista verCarrito(String correo){
		return new CarritoVista(detalleRepository.lineasDelCarrito(correo, EstadoCompra.PENDIENTE));
	}

	@Transactional
	public void agregar(String correo, Long idArticulo, int cantidad){
		if (cantidad <1){
			throw new CarritoException("La cantidad debe ser al menos de 1");
		}
		Articulo articulo = articuloRepository.findById(idArticulo)
				.filter(Articulo::isActivo)
				.orElseThrow(() -> new CarritoException(("Ese artículo no esta disponible")));

		Compra carrito = obtenerOCrearCarrito(correo);

		DetalleCompra linea = carrito.getDetalles().stream()
				.filter(d -> d.getArticulo().getIdArticulo().equals(idArticulo))
				.findFirst().orElse(null);

		int nuevaCantidad = (linea == null ? 0 : linea.getCantidad()) +cantidad;
		comprobarStock(articulo, nuevaCantidad);

		if (linea == null) {
			carrito.agregarDetalle(new DetalleCompra(articulo, nuevaCantidad, articulo.getPrecio()));
		}else {
			linea.setCantidad(nuevaCantidad);
			linea.setPrecioUnitario(articulo.getPrecio());
		}
	}

	@Transactional
	public void quitar(String correo, Long idDetalle) {
		DetalleCompra linea = lineaDelCarrito(correo, idDetalle);
		linea.getCompra().getDetalles().remove(linea); // orphanRemoval la borra
	}

	@Transactional
	public void cambiarCantidad(String correo, Long idDetalle, int cantidad) {
		if (cantidad < 1){
			quitar(correo, idDetalle);
			return;
		}
		DetalleCompra linea = lineaDelCarrito(correo, idDetalle);
		comprobarStock(linea.getArticulo(), cantidad);
		linea.setCantidad(cantidad);
		linea.setPrecioUnitario(linea.getArticulo().getPrecio());
	}

	// Simula el pago: descuenta stock y fija los precios. Si algo falla, no se cambia nada
	@Transactional
	public Long finalizar(String correo) {

		Compra carrito = compraRepository.bloquearPorEstado(correo, EstadoCompra.PENDIENTE)
				.stream().findFirst()
				.orElseThrow(() -> new CarritoException("Tu carrito está vacio"));
		if (carrito.getDetalles().isEmpty()) {
			throw new CarritoException("Tu carrito está vacío");
		}

		// Orden fijo por id de artículo para evitar interbloqueos entre compras simultáneas
		List<DetalleCompra> lineas = new ArrayList<>(carrito.getDetalles());
		lineas.sort(Comparator.comparing(d -> d.getArticulo().getIdArticulo()));

		BigDecimal total = BigDecimal.ZERO;
		for (DetalleCompra linea : lineas) {
			Articulo articulo = articuloRepository.bloquear(linea.getArticulo().getIdArticulo())
					.orElseThrow(() -> new CarritoException("Un artículo del carrito ya no existe"));
			if (!articulo.isActivo()) {
				throw new CarritoException("<<" + articulo.getNombre() + ">> ya no está disponible");
			}
			comprobarStock(articulo, linea.getCantidad());

			articulo.setStock(articulo.getStock() - linea.getCantidad());
			linea.setPrecioUnitario(articulo.getPrecio());
			total = total.add(linea.getSubtotal());
		}

		carrito.setTotal(total);
		carrito.setEstado(EstadoCompra.PAGADA);
		carrito.setFecha(LocalDateTime.now());
		return carrito.getIdCompra();

	}

	@Transactional
	public void cancelar(String correo, Long idCompra) {
		Compra compra = compraRepository.bloquearPropia(idCompra, correo).orElseThrow();
		if (compra.getEstado() != EstadoCompra.PAGADA) {
			throw new CarritoException("Solo se pueden cancelar las compras pagadas");
		}

		// Mismo orden fijo que al finalizar, para evitar interbloqueos entre operaciones simultáneas
		List<DetalleCompra> lineas = new ArrayList<>(compra.getDetalles());
		lineas.sort(Comparator.comparing(d -> d.getArticulo().getIdArticulo()));

		for (DetalleCompra linea : lineas) {
			Articulo articulo = articuloRepository.bloquear(linea.getArticulo().getIdArticulo()).orElseThrow();
			articulo.setStock(articulo.getStock() + linea.getCantidad());
		}
		compra.setEstado(EstadoCompra.CANCELADA);
	}


}
