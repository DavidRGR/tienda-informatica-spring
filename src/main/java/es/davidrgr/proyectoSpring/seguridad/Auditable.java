package es.davidrgr.proyectoSpring.seguridad;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

	String accion();

	String entidad();

	// Si se indica, el detalle guarda el valor que devuelve el metodo (por ejemplo "activo = false")
	String campo() default "";
}
