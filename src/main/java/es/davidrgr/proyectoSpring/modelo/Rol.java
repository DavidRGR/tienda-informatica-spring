package es.davidrgr.proyectoSpring.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Rol {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_rol")
	private Long idRol;

	@Column(nullable = false, unique = true, length = 50)
	private String nombre;

	public Rol(String nombre) {
		this.nombre = nombre;
	}

}
