package logica;

import java.time.DayOfWeek;
import java.time.LocalTime;

import utiles.Sexo;

public class Estudiante extends Persona{
	//Atributos
	private boolean licenciaMatricula;
	private boolean baja;
	private int grupo; // Nuevo atributo grupo
	private int guardiasCumplidas;  //Nuevo atributo guardiasCumplidas

	//Constructor
	public Estudiante (String ci, String nombre,String apellidos, Sexo sexo, boolean activo, int guardiasAsignadas , int cantidadGuardiasFestivo, int grupo){
		super(ci, nombre, apellidos, sexo, activo, guardiasAsignadas, cantidadGuardiasFestivo);
		setGrupo(grupo);
		this.guardiasAsignadas = 0;
        this.guardiasCumplidas = 0;
	}
	//Getters y setters
	public boolean isLicenciaMatricula() {
		return licenciaMatricula;
	}

	public void setLicenciaMatricula(boolean licenciaMatricula) {
		this.licenciaMatricula = licenciaMatricula;
	}
	
	public void setGuardiasCumplidas(int guardiasCumplidas){
		this.guardiasCumplidas = guardiasCumplidas;
	}
	
	public int getGuardiasCumplidas(){
		return guardiasCumplidas;
	}

	public boolean isBaja() {
		return baja;
	}

	public void setBaja(boolean baja) {
		this.baja = baja;
	}

	public void setGrupo(int grupo) {
		this.grupo = grupo;
	}

	public int getGrupo() {
		return grupo;
	}  
	//Metodos
	@Override
	public boolean puedeHacerGuardia(Horario horario) {
	    boolean puede = false;
	    if (getActivo() && horario != null) {
	        int mes = horario.getDia().getMonthValue();
	        if (mes != 7 && mes != 8) {
	            LocalTime inicio = horario.getHoraInicio();
	            LocalTime fin = horario.getHoraFin();
	            DayOfWeek dia = horario.getDia().getDayOfWeek();
	            switch (getSexo()) {
	                case MASCULINO:
	                    puede = esHorarioNocturnoValido(inicio, fin);
	                    break;
	                case FEMENINO:
	                    puede = esFinDeSemana(dia) && esHorarioDiurnoValido(inicio, fin);
	                    break;
	                default:
	                    puede = false;
	            }
	        }
	    }
	    return puede;
	}

	private boolean esHorarioNocturnoValido(LocalTime inicio, LocalTime fin) {
	    return inicio.equals(LocalTime.of(20, 0)) && 
	           fin.equals(LocalTime.of(8, 0));
	}

	private boolean esHorarioDiurnoValido(LocalTime inicio, LocalTime fin) {
	    return inicio.equals(LocalTime.of(8, 0)) && 
	           fin.equals(LocalTime.of(20, 0));
	}

	private boolean esFinDeSemana(DayOfWeek dia) {
	    return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
	}

    public void incrementarGuardiasAsignadas() {
        this.guardiasAsignadas++;
    }
    
    public void registrarGuardiaCumplida() {
        this.guardiasCumplidas++;
    }
    
    public int calcularGuardiasPendientes() {
        return guardiasAsignadas - guardiasCumplidas;
    }
    
    public boolean tieneGuardiasPendientes() {
        return calcularGuardiasPendientes() > 0;
    }
}
