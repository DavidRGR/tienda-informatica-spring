package es.davidrgr.proyectoSpring.seguridad;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LimitadorIntentos {

	// Variables para contar el numero de fallos y hsta cuando va a estar bloqueado
	private static final int MAX_FALLOS = 5;
	private static final Duration BLOQUEO = Duration.ofMinutes(15);

	//Objeto estados usando record
	private record Estado(int fallos, Instant ultimoFallo, Instant bloqueadoHasta) {}

	// Mapa clave valor de los estados
	private final Map<String, Estado> estados = new ConcurrentHashMap<>();

	// Metodo para comprobar que este bloqueado
	public boolean estaBloqueado(String clave) {
		// Obtiene el estado por su clave
		Estado e = estados.get(clave);

		// Si esta nulo devuelve falso
		if (e == null || e.bloqueadoHasta() == null){
			return false;
		}
		// Si el instante donde sucede la comprobacion es posterior al instante maximo donde esta bloqueado lo desbloquea
		if (Instant.now().isAfter(e.bloqueadoHasta)){
			// Quitamos el bloqueo
			estados.remove(clave,e);
		}

		return true;
	}

	// Metodo para registrar los fallos
	public void registrarFallo(String clave) {
		// El instante en que se registra el fallo
		Instant ahora = Instant.now();

		//Recorre el mapa de los estados (Similar a un forEach)
		estados.compute(clave, (k, e) ->{
			// Si el ultimo fallo es antiguo se empieza de cero
			boolean caducado = e == null || ahora.isAfter(e.ultimoFallo().plus(BLOQUEO));
			int fallos = (caducado ? 0 : e.fallos()) +1;

			// Calcula el numero de fallos y lo devuelve
			return fallos >= MAX_FALLOS
					? new Estado(0, ahora, ahora.plus(BLOQUEO))
					: new Estado(fallos, ahora, null);
		});
	}

	// Reinicia el contado de los estados
	public void reiniciar(String clave) {
		estados.remove(clave);
	}

}
