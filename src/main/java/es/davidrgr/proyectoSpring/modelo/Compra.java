package es.davidrgr.proyectoSpring.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compra")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"usuario", "detalles"})
public class Compra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_compra")
	private Long idCompra;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime fecha = LocalDateTime.now();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoCompra estado = EstadoCompra.PENDIENTE;

	@NotNull
	@PositiveOrZero
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal total = BigDecimal.ZERO;

	@OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DetalleCompra> detalles = new ArrayList<>();

	public Compra(Usuario usuario) {
		this.usuario = usuario;
	}

	public void agregarDetalle(DetalleCompra detalle) {
		detalles.add(detalle);
		detalle.setCompra(this);
	}
}