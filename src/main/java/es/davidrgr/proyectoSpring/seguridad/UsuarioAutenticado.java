package es.davidrgr.proyectoSpring.seguridad;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class UsuarioAutenticado extends User {

	private final String nombre;

	public UsuarioAutenticado(String correo, String passwordHash,
							  Collection<? extends GrantedAuthority> autoridades,
							  String nombre, boolean bloqueada) {
		super(correo, passwordHash, true, true, true, !bloqueada, autoridades);
		this.nombre = nombre;
	}

	public String getNombre(){
		return nombre;
	}
}
