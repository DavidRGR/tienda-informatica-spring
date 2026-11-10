package es.davidrgr.proyectoSpring.web;

import es.davidrgr.proyectoSpring.servicio.AlmacenImagenes;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AlmacenImagenes almacen;

	public WebConfig(AlmacenImagenes almacen) {
		this.almacen = almacen;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(almacen.getDirectorio().toUri().toString())
				.setCachePeriod(3600);
	}
}
