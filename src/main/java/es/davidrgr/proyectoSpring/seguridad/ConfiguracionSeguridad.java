package es.davidrgr.proyectoSpring.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

	//Se utiliza para codificar la contraseña
	@Bean
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

	//Se encarga de la configuración de seguridad y sobre cual rol puede acceder a determinadas partes de la aplicación
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		http
				//Metodo para configurar a que ruta se podra acceder con cada rol
				// Autoriza todas las carpetas publicas para que cualquiera pueda acceder
				// Asigna las carpetas /admin y sus subcarpetas para que solo usuarios con el rol de Admin puedan acceder a ellas
				// Asigna las carpetas /carrito y sus subcarpetas para que solo usuarios con el rol de Comprador puedan acceder a ellas
				//Todas las peticiones deberán autenticarse
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/","/login","/registro","/registro-admin","/articulos/**", "/css/**", "/js/**", "/img/**", "/legal/**", "/error").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/carrito/**","/compras/**").hasRole("COMPRADOR")
						.requestMatchers(HttpMethod.POST, "/articulos/*/comentarios").authenticated()
						.anyRequest().authenticated())
				//Con este metodo configuramos el login de la página
				// Asignamos la pagina de login
				//Asignamos el correo como parametro para buscar
				//En caso de ser correcto el login, redirigimos al index
				// Permitimos todas las conexiones
				.formLogin(form -> form
						.loginPage("/login")
						.usernameParameter("correo")
						.defaultSuccessUrl("/", true)
						.failureHandler((request, response, excepcion) -> {
							String destino = (excepcion instanceof LockedException) ? "/login?bloqueado" : "/login?error";
							response.sendRedirect(request.getContextPath() + destino);
						})
						.permitAll())
				//Con este metodo configuramos el logout
				//Si tiene exito redirije al index
				//Permite todas las conexiones
				.logout(logout -> logout
						.logoutSuccessUrl("/")
						.permitAll());

		//Construimos la cadena http segura y la retornamos
		return http.build();
	}

}
