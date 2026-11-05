package es.davidrgr.proyectoSpring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroAdminForm extends RegistroForm{

	@NotBlank(message = "El código de empleados es obligatorio")
	private String codigoAdmin;
}
