package es.davidrgr.proyectoSpring.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

	//Redirecciona / al index de la página
	@GetMapping("/")
	public String inicio(){
		return "index";
	}

}
