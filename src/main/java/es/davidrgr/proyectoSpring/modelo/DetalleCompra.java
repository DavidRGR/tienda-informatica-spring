package es.davidrgr.proyectoSpring.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compra")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"compra", "articulo"})
public class DetalleCompra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_detalle")
	private Long idDetalle;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_compra", nullable = false)
	private Compra compra;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_articulo", nullable = false)
	private Articulo articulo;

	@Positive
	@Column(nullable = false)
	private int cantidad;

	@NotNull
	@PositiveOrZero
	@Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
	private BigDecimal precioUnitario;

	public DetalleCompra(Articulo articulo, int cantidad, BigDecimal precioUnitario) {
		this.articulo = articulo;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
	}

	public BigDecimal getSubtotal() {
		return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
	}
}