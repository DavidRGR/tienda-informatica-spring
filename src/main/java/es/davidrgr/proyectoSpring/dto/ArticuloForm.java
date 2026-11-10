package es.davidrgr.proyectoSpring.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
public class ArticuloForm {

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 200, message = "Máximo 200 caracteres")
    private String nombre;

	@Size(max = 5000, message = "Máximo 5000 caracteres")
	private String descripcion;

	@NotNull(message = "Indica el precio")
	@DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
	@Digits(integer = 8, fraction = 2, message = "Máximo 8 enteros y 2 decimales")
	private BigDecimal precio;

	@NotNull(message = "Indica el stock")
	@Min(value = 0, message = "El stock no puede ser negativo")
	@Max(value = 1000000, message = "Stock demasiado alto")
	private Integer stock;

	@NotNull(message = "Elige una marca")
	private Long idMarca;

	@NotNull(message = "Elige una categoria")
	private Long idCategoria;

	private Long idSubcategoria;

	private boolean activo = true;

	private MultipartFile imagen;
}
