package es.davidrgr.proyectoSpring.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "subcategoria", uniqueConstraints = @UniqueConstraint(columnNames = {"id_categoria", "nombre"}))
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "categoria")
public class Subcategoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_subcategoria")
	private Long idSubcategoria;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String nombre;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_categoria", nullable = false)
	private Categoria categoria;

	public Subcategoria(String nombre, Categoria categoria) {
		this.nombre = nombre;
		this.categoria = categoria;
	}
}