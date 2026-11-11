package es.davidrgr.proyectoSpring.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
public class Auditoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_auditoria")
    private Long idAuditoria;

	@Column(nullable = false, insertable = false, updatable = false)
	private LocalDateTime fecha;

	@Column(nullable = false, length = 150)
	private String usuario;

	@Column(nullable = false, length = 50)
	private String accion;

	@Column(nullable = false, length = 50)
	private String entidad;

	@Column(name = "id_entidad")
	private Long idEntidad;

	@Column(length = 500)
	private String detalle;

	public Auditoria(String usuario, String accion, String entidad, Long idEntidad, String detalle) {
		this.usuario = usuario;
		this.accion = accion;
		this.entidad = entidad;
		this.idEntidad = idEntidad;
		this.detalle = detalle;
	}
}
