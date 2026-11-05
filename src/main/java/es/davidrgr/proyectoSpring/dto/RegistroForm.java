package es.davidrgr.proyectoSpring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroForm {

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 100, message = "Máximo 100 caracteres")
	private String nombre;

	@NotBlank(message = "El correo es obligatorio")
	@Email(message = "El correo no es válido")
	@Size(max = 150, message = "Máximo 150 caracteres")
	private String correo;

	@NotBlank(message = "La contraseña es obligatoria")
	@Size(min = 8, max = 72, message = "Debe tener entre 8 y 72 caracteres")
	private String password;

	@NotBlank(message = "Confirma la contraseña")
	private String confirmarPassword;
}
