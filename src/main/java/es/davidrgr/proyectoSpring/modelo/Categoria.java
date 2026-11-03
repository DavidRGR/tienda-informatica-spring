package es.davidrgr.proyectoSpring.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "subcategorias")
public class Categoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_categoria")
	private Long idCategoria;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, unique = true, length = 100)
	private String nombre;

	@OneToMany(mappedBy = "categoria")
	@OrderBy("nombre")
	private Set<Subcategoria> subcategorias = new LinkedHashSet<>();

	public Categoria(String nombre) {
		this.nombre = nombre;
	}
}