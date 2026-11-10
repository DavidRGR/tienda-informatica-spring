package es.davidrgr.proyectoSpring.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Controller
public class LegalController {

	private record Pagina(String titulo, List<String> parrafos) {
	}

	private static Map.Entry<String, Pagina> p(String clave, String titulo, String... parrafos) {
		return Map.entry(clave, new Pagina(titulo, List.of(parrafos)));
	}

	private static final Map<String, Pagina> PAGINAS = Map.ofEntries(
			p("como-comprar", "Cómo comprar",
					"Explora el catálogo, entra en la ficha de un artículo y pulsa «Añadir al carrito». Necesitas una cuenta de comprador para usar el carrito.",
					"Desde el carrito puedes cambiar cantidades o quitar artículos y, cuando estés listo, finalizar la compra. Es una simulación: no se realiza ningún cobro."),
			p("formas-de-pago", "Formas de pago",
					"En TiendaPC no se realizan pagos reales. Al finalizar la compra, el pedido pasa al estado «pagada» de forma automática, para demostrar el funcionamiento de la tienda."),
			p("gastos-de-envio", "Gastos de envío",
					"Como la tienda es ficticia, no se envía ningún pedido ni se aplican gastos de envío. Los plazos y costes que veas en otras tiendas reales no se corresponden con este proyecto."),
			p("preguntas-frecuentes", "Preguntas frecuentes",
					"¿Puedo comprar de verdad? No: es un proyecto de demostración.",
					"¿Puedo opinar sobre un artículo? Sí, con una cuenta. Si lo has comprado, tu opinión se marcará como «compra verificada».",
					"¿Puedo tener cuenta de comprador y de empleado? Sí, un mismo usuario puede tener los dos roles."),
			p("condiciones", "Condiciones de compra",
					"El proceso de compra de TiendaPC es una simulación: no se realiza ningún cobro ni se envía ningún pedido.",
					"El stock y los precios mostrados sirven para demostrar el funcionamiento de la tienda y pueden cambiar o reiniciarse en cualquier momento."),
			p("quienes-somos", "Quiénes somos",
					"TiendaPC es un proyecto personal creado para practicar el desarrollo de aplicaciones web con Spring Boot, Thymeleaf, Spring Security y PostgreSQL.",
					"No existe ninguna empresa detrás de este sitio."),
			p("aviso-legal", "Aviso legal",
					"TiendaPC es una tienda ficticia creada con fines educativos y de demostración. No existe ninguna empresa detrás de este sitio ni se realizan ventas reales.",
					"Los productos, precios, imágenes y opiniones que aparecen son de ejemplo. Cualquier parecido con una tienda real es coincidencia."),
			p("privacidad", "Política de privacidad",
					"Los datos que introduces al registrarte (nombre, correo y contraseña) se guardan únicamente en la base de datos de este proyecto de demostración.",
					"Las contraseñas se almacenan encriptadas y nunca en texto plano. No se comparten datos con terceros ni se usan con fines comerciales.",
					"Como es un entorno de pruebas, te recomendamos no usar datos personales reales."),
			p("cookies", "Política de cookies",
					"Este sitio utiliza únicamente la cookie de sesión necesaria para mantener tu inicio de sesión y tu carrito.",
					"No se usan cookies de publicidad ni de seguimiento."),
			p("contacto", "Contacto y ayuda",
					"Al ser un proyecto de demostración, no hay servicio de atención al cliente ni dirección de contacto real.",
					"Si has encontrado un error en la tienda, puedes comunicárselo a su autor a través del repositorio del proyecto."),
			p("devoluciones", "Devoluciones y garantías",
					"Como no se realizan ventas reales, tampoco hay devoluciones ni garantías. Todo lo que aparece en esta página es de ejemplo.")
	);

	@GetMapping("/legal/{tema}")
	public String ver(@PathVariable String tema, Model model) {
		Pagina pagina = PAGINAS.get(tema);
		if (pagina == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Página no encontrada");
		}
		model.addAttribute("titulo", pagina.titulo());
		model.addAttribute("parrafos", pagina.parrafos());
		return "legal";
	}
}