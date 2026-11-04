package es.davidrgr.proyectoSpring.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/","/login","/articulos/**", "/css/**", "/js/**", "/img/**").permitAll() //A todas estas uris pueden acceder todos los usuarios
						.requestMatchers("/admin/**").hasRole("ADMIN") //A todas las uris que comiencen por admin solo podrán acceder usuarios con el Rol de administrador
						.requestMatchers("/carrito/**").hasRole("COMPRADOR") // A todas las uris de carrito solo podrán acceder usuarios con el Rol de COMPRADOR
						.anyRequest().authenticated()) //Todas las peticiones deberán autenticarse
				.formLogin(form -> form //Con este metodo configuramos el login de la página
						.loginPage("/login") // Asignamos la pagina de login
						.usernameParameter("correo") //Asignamos el correo como parametro para buscar
						.defaultSuccessUrl("/", true) //En caso de ser correcto el login, redirigimos al index
						.permitAll()) // Permitimos todas las conexiones
				.logout(logout -> logout //Con este metodo configuramos el logout
						.logoutSuccessUrl("/") //Si tiene exito redirije al index
						.permitAll()); //Permite todas las conexiones

		return http.build(); //Construimos la cadena de seguridad y la retornamos
	}

}
