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
				//Metodo para configurar a que ruta se podra acceder con cada rol
				.authorizeHttpRequests(auth -> auth
						// Autoriza todas las carpetas publicas para que cualquiera pueda acceder
						.requestMatchers("/","/login","/registro","/registro-admin","/articulos/**", "/css/**", "/js/**", "/img/**").permitAll()
						// Asigna las carpetas /admin y sus subcarpetas para que solo usuarios con el rol de Admin puedan acceder a ellas
						.requestMatchers("/admin/**").hasRole("ADMIN")
						// Asigna las carpetas /carrito y sus subcarpetas para que solo usuarios con el rol de Comprador puedan acceder a ellas
						.requestMatchers("/carrito/**").hasRole("COMPRADOR")
						//Todas las peticiones deberán autenticarse
						.anyRequest().authenticated())
				//Con este metodo configuramos el login de la página
				.formLogin(form -> form
						// Asignamos la pagina de login
						.loginPage("/login")
						//Asignamos el correo como parametro para buscar
						.usernameParameter("correo")
						//En caso de ser correcto el login, redirigimos al index
						.defaultSuccessUrl("/", true)
						// Permitimos todas las conexiones
						.permitAll())
				//Con este metodo configuramos el logout
				.logout(logout -> logout
						//Si tiene exito redirije al index
						.logoutSuccessUrl("/")
						//Permite todas las conexiones
						.permitAll());

		//Construimos la cadena http segura y la retornamos
		return http.build();
	}

}
