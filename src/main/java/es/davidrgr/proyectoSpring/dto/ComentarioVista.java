package es.davidrgr.proyectoSpring.dto;

import java.time.LocalDateTime;

public record ComentarioVista(Long id, String autor, int valoracion, String texto, LocalDateTime fecha, Boolean compraVerificada) {
}