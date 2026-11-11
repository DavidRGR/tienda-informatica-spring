package es.davidrgr.proyectoSpring.seguridad;

import es.davidrgr.proyectoSpring.modelo.Auditoria;
import es.davidrgr.proyectoSpring.repositorio.AuditoriaRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
// Un nivel por encima del de las transacciones: el aspecto se ejecuta cuando la operación ya está confirmada
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class AuditoriaAspect {

	private final AuditoriaRepository auditoriaRepository;

	public AuditoriaAspect(AuditoriaRepository auditoriaRepository) {
		this.auditoriaRepository = auditoriaRepository;
	}

	private String usuarioActual() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
			return "sistema";
		}
		return auth.getName();
	}

	@AfterReturning(pointcut = "@annotation(auditable)", returning = "resultado")
	public void registrar(JoinPoint joinPoint, Auditable auditable, Object resultado) {
    	try {
			// El id es lo que devuelve el metodo (alta) o su primer argumento (resto de casos)
			Long idEntidad = null;
			if (resultado instanceof Long id) {
				idEntidad = id;
			} else if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof Long id) {
				idEntidad = id;
			}
			String detalle = auditable.campo().isEmpty() ? null : auditable.campo() + " = " + resultado;

			auditoriaRepository.save(new Auditoria(usuarioActual(), auditable.accion(),
					auditable.entidad(), idEntidad, detalle));
		}catch (RuntimeException e) {
			// Un fallo al registrar no debe deshacer ni romper una operación que ya se ha hecho
			log.error("No se pudo registrar la auditoria de {} {}", auditable.accion(), auditable.entidad(), e);
		}
	}
}
