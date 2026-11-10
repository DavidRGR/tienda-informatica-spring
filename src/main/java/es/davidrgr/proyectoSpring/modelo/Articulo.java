package es.davidrgr.proyectoSpring.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "articulo")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"marca", "categoria", "subcategoria"})
public class Articulo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_articulo")
	private Long idArticulo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_marca", nullable = false)
	private Marca marca;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_categoria", nullable = false)
	private Categoria categoria;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_subcategoria")
	private Subcategoria subcategoria;

	@NotBlank
	@Size(max = 200)
	@Column(nullable = false, length = 200)
	private String nombre;

	@Column(columnDefinition = "text")
	private String descripcion;

	@Size(max = 500)
	@Column(name = "imagen_url", length = 500)
	private String imagenUrl;

	@NotNull
	@PositiveOrZero
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal precio;

	@PositiveOrZero
	@Column(nullable = false)
	private int stock = 0;

	@Column(nullable = false)
	private boolean activo = true;

	@CreatedBy
	@Column(name = "creado_por", updatable = false, length = 150)
	private String creadoPor;

	@LastModifiedBy
	@Column(name = "modificado_por", length = 150)
	private String modificadoPor;

	@CreatedDate
	@Column(name = "fecha_creacion", nullable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	@LastModifiedDate
	@Column(name = "fecha_modificacion", nullable = false)
	private LocalDateTime fechaModificacion;

	public Articulo(String nombre, BigDecimal precio, Marca marca,
					Categoria categoria, Subcategoria subcategoria) {
		this.nombre = nombre;
		this.precio = precio;
		this.marca = marca;
		this.categoria = categoria;
		this.subcategoria = subcategoria;
	}
}