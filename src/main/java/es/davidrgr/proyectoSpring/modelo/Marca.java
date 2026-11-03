package es.davidrgr.proyectoSpring.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "marca")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Marca {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_marca")
	private Long idMarca;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, unique = true, length = 100)
	private String nombre;

	public Marca(String nombre) {
		this.nombre = nombre;
	}
}