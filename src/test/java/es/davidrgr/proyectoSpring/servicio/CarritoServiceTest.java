package es.davidrgr.proyectoSpring.servicio;

import es.davidrgr.proyectoSpring.modelo.*;
import es.davidrgr.proyectoSpring.repositorio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarritoServiceTest {

	private static final String CORREO = "ana@x.com";

	@Mock
	CompraRepository compraRepository;
	@Mock
	DetalleCompraRepository detalleRepository;
	@Mock
	ArticuloRepository articuloRepository;
	@Mock
	UsuarioRepository usuarioRepository;

	@InjectMocks
	CarritoService servicio;

	private Compra carrito;

	@BeforeEach
	void preparar() {
		carrito = new Compra(new Usuario("Ana", CORREO, "hash"));
		ReflectionTestUtils.setField(carrito, "idCompra", 100L);

		when(compraRepository.findFirstByUsuarioCorreoAndEstadoOrderByIdCompraAsc(CORREO, EstadoCompra.PENDIENTE))
				.thenReturn(Optional.of(carrito));
		when(compraRepository.bloquearPorEstado(CORREO, EstadoCompra.PENDIENTE)).thenReturn(List.of(carrito));
		when(compraRepository.bloquearPropia(100L, CORREO)).thenReturn(Optional.of(carrito));
	}

	// ---------------- Utilidades ----------------
	private Articulo articulo(long id, String nombre, String precio, int stock, boolean activo) {
		Articulo a = new Articulo(nombre, new BigDecimal(precio), new Marca("Marca"), new Categoria("Cat"), null);
		ReflectionTestUtils.setField(a, "idArticulo", id);
		a.setStock(stock);
		a.setActivo(activo);
		when(articuloRepository.findById(id)).thenReturn(Optional.of(a));
		when(articuloRepository.bloquear(id)).thenReturn(Optional.of(a));
		return a;
	}

	private DetalleCompra linea(Articulo articulo, int cantidad, String precioGuardado) {
		DetalleCompra d = new DetalleCompra(articulo, cantidad, new BigDecimal(precioGuardado));
		carrito.agregarDetalle(d);
		return d;
	}

	// ---------------- Añadir al carrito ----------------
	@Test
	void anadirCreaLaLineaConElPrecioActual() {
		articulo(1L, "Ratón", "10.00", 10, true);

		servicio.agregar(CORREO, 1L, 2);

		assertThat(carrito.getDetalles()).hasSize(1);
		DetalleCompra d = carrito.getDetalles().get(0);
		assertThat(d.getCantidad()).isEqualTo(2);
		assertThat(d.getPrecioUnitario()).isEqualByComparingTo("10.00");
	}

	@Test
	void anadirElMismoArticuloSumaLaCantidadEnLaMismaLinea() {
		articulo(1L, "Ratón", "10.00", 10, true);

		servicio.agregar(CORREO, 1L, 2);
		servicio.agregar(CORREO, 1L, 3);

		assertThat(carrito.getDetalles()).hasSize(1);
		assertThat(carrito.getDetalles().get(0).getCantidad()).isEqualTo(5);
	}

	@Test
	void anadirRechazaCantidadesMenoresQueUno() {
		articulo(1L, "Ratón", "10.00", 10, true);

		assertThatThrownBy(() -> servicio.agregar(CORREO, 1L, 0)).isInstanceOf(CarritoException.class);
		assertThat(carrito.getDetalles()).isEmpty();
	}

	@Test
	void anadirRechazaPasarseDelStockContandoLoQueYaHayEnElCarrito() {
		articulo(1L, "Ratón", "10.00", 4, true);

		servicio.agregar(CORREO, 1L, 3);

		assertThatThrownBy(() -> servicio.agregar(CORREO, 1L, 2))
				.isInstanceOf(CarritoException.class)
				.hasMessageContaining("Solo quedan 4");
		assertThat(carrito.getDetalles().get(0).getCantidad()).isEqualTo(3);
	}

	@Test
	void anadirRechazaUnArticuloDesactivado() {
		articulo(1L, "Ratón", "10.00", 10, false);

		assertThatThrownBy(() -> servicio.agregar(CORREO, 1L, 1)).isInstanceOf(CarritoException.class);
	}

	// ---------------- Cambiar cantidad y quitar ----------------
	@Test
	void cambiarCantidadActualizaLaLinea() {
		Articulo a = articulo(1L, "Ratón", "10.00", 10, true);
		DetalleCompra d = linea(a, 2, "10.00");
		when(detalleRepository.findByIdDetalleAndCompraUsuarioCorreoAndCompraEstado(7L, CORREO, EstadoCompra.PENDIENTE))
				.thenReturn(Optional.of(d));

		servicio.cambiarCantidad(CORREO, 7L, 6);

		assertThat(d.getCantidad()).isEqualTo(6);
	}

	@Test
	void cambiarCantidadRechazaPasarseDelStock() {
		Articulo a = articulo(1L, "Ratón", "10.00", 5, true);
		DetalleCompra d = linea(a, 2, "10.00");
		when(detalleRepository.findByIdDetalleAndCompraUsuarioCorreoAndCompraEstado(7L, CORREO, EstadoCompra.PENDIENTE))
				.thenReturn(Optional.of(d));

		assertThatThrownBy(() -> servicio.cambiarCantidad(CORREO, 7L, 9)).isInstanceOf(CarritoException.class);
		assertThat(d.getCantidad()).isEqualTo(2);
	}

	@Test
	void cambiarLaCantidadACeroQuitaLaLinea() {
		Articulo a = articulo(1L, "Ratón", "10.00", 10, true);
		DetalleCompra d = linea(a, 2, "10.00");
		when(detalleRepository.findByIdDetalleAndCompraUsuarioCorreoAndCompraEstado(7L, CORREO, EstadoCompra.PENDIENTE))
				.thenReturn(Optional.of(d));

		servicio.cambiarCantidad(CORREO, 7L, 0);

		assertThat(carrito.getDetalles()).isEmpty();
	}

	@Test
	void noSePuedeQuitarUnaLineaQueNoEsDelCarritoDelUsuario() {
		// El repositorio solo devuelve la línea si es del carrito pendiente de ese usuario
		when(detalleRepository.findByIdDetalleAndCompraUsuarioCorreoAndCompraEstado(99L, CORREO, EstadoCompra.PENDIENTE))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.quitar(CORREO, 99L)).isInstanceOf(CarritoException.class);
	}

	// ---------------- Finalizar la compra ----------------
	@Test
	void finalizarDescuentaElStockFijaLosPreciosYMarcaLaCompraComoPagada() {
		Articulo a1 = articulo(1L, "Ratón", "10.00", 5, true);
		Articulo a2 = articulo(2L, "Teclado", "20.50", 3, true);
		// El primer artículo se guardó en el carrito a 9,00 y ahora cuesta 10,00
		DetalleCompra l1 = linea(a1, 2, "9.00");
		DetalleCompra l2 = linea(a2, 1, "20.50");

		Long idCompra = servicio.finalizar(CORREO);

		assertThat(idCompra).isEqualTo(100L);
		assertThat(a1.getStock()).isEqualTo(3);
		assertThat(a2.getStock()).isEqualTo(2);
		assertThat(l1.getPrecioUnitario()).isEqualByComparingTo("10.00");
		assertThat(carrito.getTotal()).isEqualByComparingTo("40.50");
		assertThat(carrito.getEstado()).isEqualTo(EstadoCompra.PAGADA);
	}

	@Test
	void finalizarUnCarritoVacioEsUnError() {
		assertThatThrownBy(() -> servicio.finalizar(CORREO))
				.isInstanceOf(CarritoException.class)
				.hasMessageContaining("vacío");
	}

	@Test
	void finalizarSinStockSuficienteFallaYLaCompraSigueSiendoUnCarrito() {
		Articulo a = articulo(1L, "Ratón", "10.00", 1, true);
		linea(a, 2, "10.00");

		assertThatThrownBy(() -> servicio.finalizar(CORREO)).isInstanceOf(CarritoException.class);
		assertThat(carrito.getEstado()).isEqualTo(EstadoCompra.PENDIENTE);
		assertThat(a.getStock()).isEqualTo(1);
	}

	@Test
	void finalizarRechazaUnArticuloQueSeHaDesactivadoMientrasTanto() {
		Articulo a = articulo(1L, "Ratón", "10.00", 5, false);
		linea(a, 1, "10.00");

		assertThatThrownBy(() -> servicio.finalizar(CORREO)).isInstanceOf(CarritoException.class);
		assertThat(carrito.getEstado()).isEqualTo(EstadoCompra.PENDIENTE);
	}

	// ---------------- Cancelar una compra ----------------
	@Test
	void cancelarDevuelveElStockYMarcaLaCompraComoCancelada() {
		Articulo a1 = articulo(1L, "Ratón", "10.00", 3, true);
		Articulo a2 = articulo(2L, "Teclado", "20.50", 2, true);
		linea(a1, 2, "10.00");
		linea(a2, 1, "20.50");
		carrito.setEstado(EstadoCompra.PAGADA);

		servicio.cancelar(CORREO, 100L);

		assertThat(a1.getStock()).isEqualTo(5);
		assertThat(a2.getStock()).isEqualTo(3);
		assertThat(carrito.getEstado()).isEqualTo(EstadoCompra.CANCELADA);
	}

	@Test
	void soloSePuedenCancelarLasComprasPagadas() {
		Articulo a = articulo(1L, "Ratón", "10.00", 3, true);
		linea(a, 2, "10.00"); // sigue PENDIENTE: es un carrito

		assertThatThrownBy(() -> servicio.cancelar(CORREO, 100L)).isInstanceOf(CarritoException.class);
		assertThat(a.getStock()).isEqualTo(3);
	}

	@Test
	void cancelarDosVecesNoDevuelveElStockDosVeces() {
		Articulo a = articulo(1L, "Ratón", "10.00", 3, true);
		linea(a, 2, "10.00");
		carrito.setEstado(EstadoCompra.PAGADA);

		servicio.cancelar(CORREO, 100L);

		assertThatThrownBy(() -> servicio.cancelar(CORREO, 100L)).isInstanceOf(CarritoException.class);
		assertThat(a.getStock()).isEqualTo(5);
	}

	@Test
	void noSePuedeCancelarLaCompraDeOtroUsuario() {
		// Una compra ajena no se encuentra: el repositorio filtra por el correo del usuario
		when(compraRepository.bloquearPropia(555L, CORREO)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.cancelar(CORREO, 555L)).isInstanceOf(NoSuchElementException.class);
	}
}
