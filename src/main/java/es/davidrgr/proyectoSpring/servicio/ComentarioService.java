package es.davidrgr.proyectoSpring.servicio;

import es.davidrgr.proyectoSpring.modelo.Articulo;
import es.davidrgr.proyectoSpring.modelo.Comentario;
import es.davidrgr.proyectoSpring.modelo.Usuario;
import es.davidrgr.proyectoSpring.repositorio.ArticuloRepository;
import es.davidrgr.proyectoSpring.repositorio.ComentarioRepository;
import es.davidrgr.proyectoSpring.repositorio.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComentarioService {

    //Inyectamos las clases que vamos a necesitar
	private final ComentarioRepository comentarioRepository;
	private final ArticuloRepository articuloRepository;
	private final UsuarioRepository usuarioRepository;

	public ComentarioService(ComentarioRepository comentarioRepository, ArticuloRepository articuloRepository, UsuarioRepository usuarioRepository) {
		this.comentarioRepository = comentarioRepository;
		this.articuloRepository = articuloRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public void crear(String correo, Long idArticulo, int valoracion, String texto) {
		if (valoracion < 1 || valoracion > 5) {
			throw new ComentarioException("Elige una valoración del 1 a 5 estrellas");
		}

		String limpio = texto == null ? "" : texto.trim();

		if (limpio.isEmpty()){
			throw  new ComentarioException("Escribe tu opinión");
		}

		if (limpio.length() > 1000) {
			throw new ComentarioException("El comentario no puede superar los 1000 caracteres");
		}

		Articulo articulo = articuloRepository.findById(idArticulo)
				.filter(Articulo::isActivo)
				.orElseThrow(() -> new ComentarioException("Articulo no disponible"));

		if (comentarioRepository.existsByArticuloIdArticuloAndUsuarioCorreo(idArticulo, correo)) {
			throw new ComentarioException("Ya has opinado sobre este artículo");
		}

		Usuario usuario = usuarioRepository.findByCorreo(correo)
				.orElseThrow(() -> new ComentarioException("Usuario no encontrado"));

		try {
			comentarioRepository.saveAndFlush(new Comentario(articulo, usuario, valoracion, limpio));
		}catch (DataIntegrityViolationException e){
			// Respaldo de la restricción UNIQUE (por ejemplo, doble clic en "Publicar")
			throw new ComentarioException("Ya has opinado sobre este artículo");
		}

	}

}
