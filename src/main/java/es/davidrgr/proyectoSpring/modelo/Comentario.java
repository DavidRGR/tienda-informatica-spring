package es.davidrgr.proyectoSpring.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentario")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"articulo", "usuario"})
public class Comentario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_comentario")
	private Long idComentario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_articulo", nullable = false)
	private Articulo articulo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;

	@Min(1)
	@Max(5)
	@Column(nullable = false)
	private int valoracion;

	@NotBlank
	@Size(max = 1000)
	@Column(nullable = false, columnDefinition = "text")
	private String texto;

	@Column(nullable = false, insertable = false, updatable = false)
	private LocalDateTime fecha;

	@Column(nullable = false)
	private boolean visible = true;

	public Comentario(Articulo articulo, Usuario usuario, int valoracion, String texto) {
		this.articulo = articulo;
		this.usuario = usuario;
		this.valoracion = valoracion;
		this.texto = texto;
	}
}
