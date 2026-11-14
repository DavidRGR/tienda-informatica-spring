package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.servicio.AlmacenImagenes;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Bean que necesitan WebConfig y AdminArticuloController en las pruebas web.
 * Usa una carpeta temporal, para no tocar la carpeta "uploads" real.
 */
@TestConfiguration
public class ConfiguracionPruebasWeb {

	@Bean
	public AlmacenImagenes almacenImagenes() throws IOException {
		// Si tu constructor solo recibe la ruta, quita el segundo argumento
		return new AlmacenImagenes(Files.createTempDirectory("uploads-test").toString(), DataSize.ofMegabytes(5));
	}
}
