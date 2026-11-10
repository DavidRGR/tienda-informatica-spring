package es.davidrgr.proyectoSpring.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditoriaConfig {

	@Bean
	public AuditorAware<String> auditorAware() {
		return () -> {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
				return Optional.empty();
			}
			// Solo se registra a los empleados. Cuando un comprador compra, el stock del artículo
			// cambia, pero eso no debe aparecer como "modificado por" ese comprador
			boolean admin = auth.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			return admin ? Optional.of(auth.getName()) : Optional.empty();
		};
	}
}
