package es.davidrgr.proyectoSpring.seguridad;

import es.davidrgr.proyectoSpring.repositorio.UsuarioRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class EscuchaAutenticacion {

	// Inyectamos el limitador de intentos y el usuario repository
	private final LimitadorIntentos limitador;
	private final UsuarioRepository usuarioRepository;

	public EscuchaAutenticacion(LimitadorIntentos limitador, UsuarioRepository usuarioRepository) {
		this.limitador = limitador;
		this.usuarioRepository = usuarioRepository;
	}

	// Escuchador el evento del fallo en la autenticacion y registra el fallo
	@EventListener
	public void alFallar(AuthenticationFailureBadCredentialsEvent evento) {
		String correo = evento.getAuthentication().getName().trim().toLowerCase(Locale.ROOT);
		// Solo se cuentan fallos de cuentas que existen, para que el mapa no crezca con correos inventados
		if (usuarioRepository.existsByCorreo(correo)){
			limitador.registrarFallo(correo);
		}
	}

	// Escucha el evento del acierto en la autenticacion y reinicia el contador a 0
	@EventListener
	public void alAcertar(AuthenticationSuccessEvent evento){
		limitador.reiniciar(evento.getAuthentication().getName().trim().toLowerCase(Locale.ROOT));
	}
}
