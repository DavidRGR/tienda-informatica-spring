package es.davidrgr.proyectoSpring.seguridad;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LimitadorIntentosTest {

	private static final String CUENTA = "ana@x.com";

	private final LimitadorIntentos limitador = new LimitadorIntentos();

	private void fallar(String cuenta, int veces) {
		for (int i = 0; i < veces; i++) {
			limitador.registrarFallo(cuenta);
		}
	}

	@Test
	void unaCuentaSinFallosNoEstaBloqueada() {
		assertThat(limitador.estaBloqueado(CUENTA)).isFalse();
	}

	@Test
	void conCuatroFallosTodaviaNoSeBloquea() {
		fallar(CUENTA, 4);
		assertThat(limitador.estaBloqueado(CUENTA)).isFalse();
	}

	@Test
	void alQuintoFalloSeBloquea() {
		fallar(CUENTA, 5);
		assertThat(limitador.estaBloqueado(CUENTA)).isTrue();
	}

	@Test
	void elBloqueoEsIndependienteParaCadaCuenta() {
		fallar(CUENTA, 5);
		assertThat(limitador.estaBloqueado("otra@x.com")).isFalse();
	}

	@Test
	void reiniciarBorraLosFallosAcumulados() {
		fallar(CUENTA, 4);
		limitador.reiniciar(CUENTA);
		// Si el contador no se hubiera reiniciado, este quinto fallo bloquearía la cuenta
		limitador.registrarFallo(CUENTA);
		assertThat(limitador.estaBloqueado(CUENTA)).isFalse();
	}
}
