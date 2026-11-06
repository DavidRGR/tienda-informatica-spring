package es.davidrgr.proyectoSpring.controllers;

import es.davidrgr.proyectoSpring.dto.RegistroAdminForm;
import es.davidrgr.proyectoSpring.dto.RegistroForm;
import es.davidrgr.proyectoSpring.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Controller
public class RegistroController {

	//Obtiene el usuarioService
	private final UsuarioService usuarioService;
	// Accede al codigo del admin
	private final String codigoAdmin;

	public RegistroController(UsuarioService usuarioService,
							  @Value("${app.admin-code}") String codigoAdmin) {
		this.usuarioService = usuarioService;
		this.codigoAdmin = codigoAdmin;
	}

	// --------------------- Utilidades ---------------------------
	private void validarComun(RegistroForm form, BindingResult result) {
		if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmarPassword())) {
			result.rejectValue("confirmarPassword", "noCoincide", "Las contraseñas no coinciden");
		}
	}

	private String procesarResultado(UsuarioService.ResultadoRegistro resultado, BindingResult result) {
		switch (resultado) {
			case PASSWORD_INCORRECTA -> {
				result.rejectValue("password", "passwordIncorrecta",
						"Ese correo ya tiene cuenta. Para añadirle este rol, introduce su contraseña actual");
				return "registro";
			}
			case YA_TIENE_ROL -> {
				result.rejectValue("correo", "yaTieneRol", "Esa cuenta ya tiene este rol");
				return "registro";
			}
			case ROL_ANADIDO -> {
				return "redirect:/login?rolAnadido";
			}
			case BLOQUEADO -> {
				result.rejectValue("password", "bloqueado",
						"Demasiados intentos fallidos. Inténtalo de nuevo en 15 minutos");
				return "registro";
			}
			default -> {
				return "redirect:/login?registrado";
			}
		}
	}

	private boolean codigoValido(String introducido) {
		return MessageDigest.isEqual(
				codigoAdmin.getBytes(StandardCharsets.UTF_8),
				introducido.getBytes(StandardCharsets.UTF_8));
	}

	// ----------------------Registro de compradores----------------
	@GetMapping("/registro")
	public String formulario(Model model) {
		model.addAttribute("registroForm", new RegistroForm());
		model.addAttribute("modoAdmin", false);
		return "registro";
	}

	@PostMapping("/registro")
	public String registrar(@Valid @ModelAttribute("registroForm") RegistroForm form,
							BindingResult result, Model model) {
		model.addAttribute("modoAdmin", false);
		validarComun(form, result);
		if (result.hasErrors()) {
			return "registro";
		}
		return procesarResultado(usuarioService.registrarOAsignarRol(form, "COMPRADOR"), result);
	}

	// --------------------Registro de administradores (empleados) ----------------
	@GetMapping("/registro-admin")
	public String formularioAdmin(Model model) {
		model.addAttribute("registroForm", new RegistroAdminForm());
		model.addAttribute("modoAdmin", true);
		return "registro";
	}

	@PostMapping("/registro-admin")
	public String registrarAdmin(@Valid @ModelAttribute("registroForm") RegistroAdminForm form,
								 BindingResult result, Model model) {
		model.addAttribute("modoAdmin", true);
		validarComun(form, result);
		if (!result.hasFieldErrors("codigoAdmin") && !codigoValido(form.getCodigoAdmin())) {
			result.rejectValue("codigoAdmin", "codigoIncorrecto", "Código de empleados incorrecto");
		}
		if (result.hasErrors()) {
			return "registro";
		}
		return procesarResultado(usuarioService.registrarOAsignarRol(form, "ADMIN"), result);
	}

}