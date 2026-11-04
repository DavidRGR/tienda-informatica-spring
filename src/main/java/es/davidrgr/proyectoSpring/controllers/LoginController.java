package es.davidrgr.proyectoSpring.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

	// Redirecciona al login de la página
	@GetMapping("/login")
	public String login(){
		return "login";
	}
}
