package es.davidrgr.proyectoSpring.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"passwordHash", "roles"})
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_usuario")
	private Long idUsuario;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String nombre;

	@NotBlank
	@Email
	@Size(max = 150)
	@Column(nullable = false, unique = true, length = 150)
	private String correo;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "usuario_rol",
			joinColumns = @JoinColumn(name = "id_usuario"),
			inverseJoinColumns = @JoinColumn(name = "id_rol")
	)
	private Set<Rol> roles = new HashSet<>();

	public Usuario(String nombre, String correo, String passwordHash) {
		this.nombre = nombre;
		this.correo = correo;
		this.passwordHash = passwordHash;
	}
}