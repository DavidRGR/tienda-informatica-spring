package es.davidrgr.proyectoSpring.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarritoVistaTest {

	private LineaCarrito linea(int cantidad, int stock, String precio) {
		return new LineaCarrito(1L, 10L, "Artículo", "Marca", null, cantidad, stock, new BigDecimal(precio));
	}

	@Test
	void elSubtotalEsPrecioPorCantidad() {
		assertThat(linea(3, 10, "19.99").subtotal()).isEqualByComparingTo("59.97");
	}

	@Test
	void unCarritoSinLineasEstaVacio() {
		CarritoVista vista = new CarritoVista(List.of());
		assertThat(vista.vacio()).isTrue();
		assertThat(vista.unidades()).isZero();
		assertThat(vista.total()).isEqualByComparingTo("0");
	}

	@Test
	void sumaUnidadesYTotalDeTodasLasLineas() {
		CarritoVista vista = new CarritoVista(List.of(linea(2, 10, "10.00"), linea(1, 5, "20.50")));
		assertThat(vista.unidades()).isEqualTo(3);
		assertThat(vista.total()).isEqualByComparingTo("40.50");
		assertThat(vista.vacio()).isFalse();
	}

	@Test
	void detectaLasLineasQueSuperanElStock() {
		assertThat(new CarritoVista(List.of(linea(2, 10, "10.00"))).hayProblemasDeStock()).isFalse();
		assertThat(new CarritoVista(List.of(linea(2, 10, "10.00"), linea(4, 3, "5.00"))).hayProblemasDeStock()).isTrue();
	}
}
