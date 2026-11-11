package es.davidrgr.proyectoSpring.dto;

import java.time.LocalDateTime;

public record ComentarioAdmin(Long id, Long idArticulo, String articulo, boolean articuloActivo, String autor, int valoracion, String texto, LocalDateTime fecha, boolean visible) {
}