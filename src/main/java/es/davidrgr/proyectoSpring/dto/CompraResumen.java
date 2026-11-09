package es.davidrgr.proyectoSpring.dto;

import es.davidrgr.proyectoSpring.modelo.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompraResumen(Long id, LocalDateTime fecha, BigDecimal total, EstadoCompra estado, long unidades) {

}
