package logica;

import java.time.LocalDate;
//Yo la queria, que un segundo con ella...
// valia con 756mil años lejos de ella...
public class DiaFestivo {
	//Atributos
    private LocalDate fecha;
    private String descripcion;

    //Contructor
    public DiaFestivo(LocalDate fecha, String descripcion) {
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    //Setters y Getters
    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaString() {
        return fecha.toString();
    }

    //Metodos
    
}
