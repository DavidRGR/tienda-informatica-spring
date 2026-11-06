package es.davidrgr.proyectoSpring.util;

public final class Imagenes {

	private Imagenes(){}

	// Devuelve la ruta lista para usar en el src de la imagen
	public static String resolver(String imagenUrl){
		if (imagenUrl == null || imagenUrl.isBlank()){
			return "/img/sin-imagen.svg";
		}
		String url = imagenUrl.trim();
		if (url.startsWith("http") || url.startsWith("/")){
			return url;
		}
		return "/img/" + url;
	}
}
