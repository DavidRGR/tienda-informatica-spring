package es.davidrgr.proyectoSpring.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ArticuloAdmin(Long id, String nombre, String marca, String categoria, BigDecimal precio, int stock, boolean activo, String modificadoPor, LocalDateTime fechaModificacion) {
}
