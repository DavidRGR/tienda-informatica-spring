package es.davidrgr.proyectoSpring.dto;

public record ResumenValoracion (Double media, Long total){

	public boolean hayValoraciones() {
		return total != null && total > 0;
	}

	public double mediaOCero() {
		return media == null ? 0 : media;
	}
}
