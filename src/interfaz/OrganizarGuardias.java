package interfaz;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import logica.Horario;
import logica.Persona;
import logica.PlanificadorGuardias;

import com.toedter.calendar.JDateChooser;

public class OrganizarGuardias extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JComboBox<String> cbPersonas;
    private ArrayList<Persona> personasFiltradas;
    private PlanificadorGuardias planificador; // Debe estar declarado aquí

    // Elimina: private JTextField txtDia;
    private JDateChooser dateChooser;
    private JLabel lblHorarioSugerido;

    // Hacer la tabla y el modelo estáticos para que persistan entre instancias
    private static DefaultTableModel modeloTablaGuardias;
    private static JTable tablaGuardias;
    private JComboBox<String> cbTurnoTrabajador;

    // Alternancia de fines de semana: true=trabajadores, false=mujeres estudiantes
    private static boolean finDeSemanaTrabajadores = true;
    private static java.time.LocalDate ultimoFinDeSemana = null;

    private void actualizarPersonasFiltradas() {
        // Encontrar el mínimo de cantidad de guardias entre todas las personas activas y profesores incorporados SOLO el día de incorporación
        int minGuardias = Integer.MAX_VALUE;
        java.util.Date fechaDate = dateChooser != null ? dateChooser.getDate() : null;
        java.time.LocalDate fechaSeleccionada = null;
        if (fechaDate != null) {
            fechaSeleccionada = fechaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        for (Persona p : planificador.getPersonas()) {
            boolean mostrar = false;
            if (p.estaActivo()) {
                mostrar = true;
            } else if (p instanceof logica.Trabajador && fechaSeleccionada != null) {
                java.time.LocalDate fechaIncorporacion = ((logica.Trabajador) p).getFechaDeIncorporacion();
                if (fechaIncorporacion != null && !fechaSeleccionada.isBefore(fechaIncorporacion)) {
                    // Activar profesor si la fecha seleccionada es igual o mayor a la de incorporación
                    p.setActivo(true);
                    mostrar = true;
                } else {
                    // Si la fecha seleccionada es anterior, asegurarse que esté inactivo
                    p.setActivo(false);
                }
            }
            if (mostrar && p.getCantidadGuardias() < minGuardias) {
                minGuardias = p.getCantidadGuardias();
            }
        }
        // Filtrar solo las personas activas o profesores incorporados con la cantidad mínima de guardias
        personasFiltradas.clear();
        cbPersonas.removeAllItems();
        for (Persona p : planificador.getPersonas()) {
            boolean mostrar = false;
            if (p.estaActivo()) {
                mostrar = true;
            } else if (p instanceof logica.Trabajador && fechaSeleccionada != null) {
                java.time.LocalDate fechaIncorporacion = ((logica.Trabajador) p).getFechaDeIncorporacion();
                if (fechaIncorporacion != null && !fechaSeleccionada.isBefore(fechaIncorporacion)) {
                    // Activar profesor si la fecha seleccionada es igual o mayor a la de incorporación
                    p.setActivo(true);
                    mostrar = true;
                } else {
                    // Si la fecha seleccionada es anterior, asegurarse que esté inactivo
                    p.setActivo(false);
                }
            }
            if (mostrar && p.getCantidadGuardias() == minGuardias) {
                personasFiltradas.add(p);
                cbPersonas.addItem(p.getNombre());
            }
        }
    }

    public OrganizarGuardias(final PlanificadorGuardias planificador) {
    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.planificador = planificador;
        setBounds(100, 100, 750, 600);
        setLocationRelativeTo(null); // Centra la ventana en la pantalla
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel panelForm = new JPanel(new GridLayout(0, 2, 10, 10));

        personasFiltradas = new ArrayList<Persona>();
        cbPersonas = new JComboBox<String>();

        panelForm.add(new JLabel("Persona:"));
        panelForm.add(cbPersonas);

        dateChooser = new com.toedter.calendar.JDateChooser();
        dateChooser.getCalendarButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            }
        });

        panelForm.add(new JLabel("Fecha:"));
        panelForm.add(dateChooser);

        // Label para mostrar el horario sugerido
        lblHorarioSugerido = new JLabel("Horario sugerido: ");
        panelForm.add(new JLabel(""));
        panelForm.add(lblHorarioSugerido);

        JButton btnAsignar = new JButton("Asignar Guardia");
        panelForm.add(btnAsignar);

        cbTurnoTrabajador = new JComboBox<String>();
        cbTurnoTrabajador.addItem("9:00 - 14:00");
        cbTurnoTrabajador.addItem("14:00 - 19:00");
        cbTurnoTrabajador.setEnabled(false);

        panelForm.add(new JLabel("Turno trabajador:"));
        panelForm.add(cbTurnoTrabajador);

        contentPane.add(panelForm, BorderLayout.CENTER);

        // Tabla para mostrar guardias asignadas
        String[] columnas = {"Persona", "CI", "Tipo", "G�nero", "Fecha", "Horario"};
        if (modeloTablaGuardias == null) {
            modeloTablaGuardias = new DefaultTableModel(columnas, 0) {
                private static final long serialVersionUID = 1L;
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        }
        if (tablaGuardias == null) {
            tablaGuardias = new JTable(modeloTablaGuardias);
        }
        JScrollPane scrollTabla = new JScrollPane(tablaGuardias);
        contentPane.add(scrollTabla, BorderLayout.SOUTH);

        // Inicializa la lista de personas filtradas
        actualizarPersonasFiltradas();

        // Actualiza la lista de personas filtradas cuando cambia la fecha
        dateChooser.getDateEditor().addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if ("date".equals(evt.getPropertyName())) {
                    actualizarPersonasFiltradas();
                    mostrarHorarioSugerido();
                    actualizarTurnoTrabajador();
                }
            }
        });

        // Actualiza el horario sugerido cuando cambia la persona o la fecha
        ActionListener actualizarHorarioListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mostrarHorarioSugerido();
                actualizarTurnoTrabajador();
            }
        };
        cbPersonas.addActionListener(actualizarHorarioListener);

        dateChooser.getDateEditor().addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if ("date".equals(evt.getPropertyName())) {
                    mostrarHorarioSugerido();
                    actualizarTurnoTrabajador();
                }
            }
        });

        btnAsignar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int idx = cbPersonas.getSelectedIndex();
                if (idx == -1) {
                    JOptionPane.showMessageDialog(null, "Seleccione una persona.");
                    return;
                }
                Persona persona = personasFiltradas.get(idx);

                try {
                    java.util.Date fechaDate = dateChooser.getDate();
                    if (fechaDate == null) {
                        JOptionPane.showMessageDialog(null, "Seleccione una fecha válida.");
                        return;
                    }
                    java.time.LocalDate fecha = fechaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    java.time.DayOfWeek dayOfWeek = fecha.getDayOfWeek();
                    utiles.Dia dia = utiles.Dia.valueOf(dayOfWeek.name());

                    java.time.LocalTime horaInicio = null;
                    java.time.LocalTime horaFin = null;
                    boolean esFestivo = false;

                    // --- Lógica para asignación según tipo y reglas ---
                    if (persona instanceof logica.Estudiante) {
                        // Estudiante varón: cualquier día, horario fijo noche
                        if (((logica.Estudiante) persona).getSexo() == utiles.Sexo.MASCULINO) {
                            horaInicio = java.time.LocalTime.of(20, 0);
                            horaFin = java.time.LocalTime.of(8, 0);
                        } else {
                            // Estudiante mujer: solo fines de semana, alternando con trabajadores
                            if (dia != utiles.Dia.SATURDAY && dia != utiles.Dia.SUNDAY) {
                                JOptionPane.showMessageDialog(null, "Las estudiantes mujeres solo pueden hacer guardias los fines de semana.");
                                return;
                            }
                            // Alternancia de fines de semana
                            java.time.LocalDate sabado = fecha.with(java.time.DayOfWeek.SATURDAY);
                            if (ultimoFinDeSemana == null || !sabado.equals(ultimoFinDeSemana)) {
                                // Revisar si ya hay asignaciones para ese fin de semana
                                boolean hayAsignacion = false;
                                for (int i = 0; i < modeloTablaGuardias.getRowCount(); i++) {
                                    String fechaFila = (String) modeloTablaGuardias.getValueAt(i, 4);
                                    java.time.LocalDate fechaGuardia = java.time.LocalDate.parse(fechaFila);
                                    java.time.DayOfWeek dow = fechaGuardia.getDayOfWeek();
                                    java.time.LocalDate sabadoFila = fechaGuardia.with(java.time.DayOfWeek.SATURDAY);
                                    if (sabadoFila.equals(sabado) && (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY)) {
                                        hayAsignacion = true;
                                        String tipoFila = (String) modeloTablaGuardias.getValueAt(i, 2);
                                        if ("Trabajador".equals(tipoFila)) {
                                            finDeSemanaTrabajadores = true;
                                        } else if ("Estudiante".equals(tipoFila)) {
                                            String generoFila = (String) modeloTablaGuardias.getValueAt(i, 3);
                                            if ("FEMENINO".equalsIgnoreCase(generoFila)) {
                                                finDeSemanaTrabajadores = false;
                                            }
                                        }
                                        break;
                                    }
                                }
                                if (!hayAsignacion) {
                                    finDeSemanaTrabajadores = !finDeSemanaTrabajadores;
                                }
                                ultimoFinDeSemana = sabado;
                            }
                            if (finDeSemanaTrabajadores) {
                                JOptionPane.showMessageDialog(null, "Este fin de semana solo pueden asignarse trabajadores.");
                                return;
                            }
                            // Turno único para mujeres estudiantes en fin de semana
                            horaInicio = java.time.LocalTime.of(8, 0);
                            horaFin = java.time.LocalTime.of(20, 0);
                        }
                    } else if (persona instanceof logica.Trabajador) {
                        // Trabajador: solo fines de semana, alternando con mujeres estudiantes
                        if (dia != utiles.Dia.SATURDAY && dia != utiles.Dia.SUNDAY) {
                            JOptionPane.showMessageDialog(null, "Los trabajadores solo pueden hacer guardias los fines de semana.");
                            return;
                        }
                        // Alternancia de fines de semana
                        java.time.LocalDate sabado = fecha.with(java.time.DayOfWeek.SATURDAY);
                        if (ultimoFinDeSemana == null || !sabado.equals(ultimoFinDeSemana)) {
                            boolean hayAsignacion = false;
                            for (int i = 0; i < modeloTablaGuardias.getRowCount(); i++) {
                                String fechaFila = (String) modeloTablaGuardias.getValueAt(i, 4);
                                java.time.LocalDate fechaGuardia = java.time.LocalDate.parse(fechaFila);
                                java.time.DayOfWeek dow = fechaGuardia.getDayOfWeek();
                                java.time.LocalDate sabadoFila = fechaGuardia.with(java.time.DayOfWeek.SATURDAY);
                                if (sabadoFila.equals(sabado) && (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY)) {
                                    hayAsignacion = true;
                                    String tipoFila = (String) modeloTablaGuardias.getValueAt(i, 2);
                                    if ("Trabajador".equals(tipoFila)) {
                                        finDeSemanaTrabajadores = true;
                                    } else if ("Estudiante".equals(tipoFila)) {
                                        String generoFila = (String) modeloTablaGuardias.getValueAt(i, 3);
                                        if ("FEMENINO".equalsIgnoreCase(generoFila)) {
                                            finDeSemanaTrabajadores = false;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (!hayAsignacion) {
                                finDeSemanaTrabajadores = !finDeSemanaTrabajadores;
                            }
                            ultimoFinDeSemana = sabado;
                        }
                        if (!finDeSemanaTrabajadores) {
                            JOptionPane.showMessageDialog(null, "Este fin de semana solo pueden asignarse estudiantes mujeres.");
                            return;
                        }
                        // Selección de turno
                        cbTurnoTrabajador.setEnabled(true);
                        String turno = (String) cbTurnoTrabajador.getSelectedItem();
                        if ("9:00 - 14:00".equals(turno)) {
                            horaInicio = java.time.LocalTime.of(9, 0);
                            horaFin = java.time.LocalTime.of(14, 0);
                        } else {
                            horaInicio = java.time.LocalTime.of(14, 0);
                            horaFin = java.time.LocalTime.of(19, 0);
                        }
                    }

                    if (horaInicio == null || horaFin == null) {
                        JOptionPane.showMessageDialog(null, "No hay horario válido para la persona y fecha seleccionada.");
                        return;
                    }

                    Horario horario = new Horario(dia, fecha, horaInicio, horaFin, esFestivo);

                    // Validar si ya existe un estudiante con el mismo horario en el mismo día
                    if (persona instanceof logica.Estudiante) {
                        for (int i = 0; i < modeloTablaGuardias.getRowCount(); i++) {
                            String tipoFila = (String) modeloTablaGuardias.getValueAt(i, 2);
                            String fechaFila = (String) modeloTablaGuardias.getValueAt(i, 4);
                            String horarioFila = (String) modeloTablaGuardias.getValueAt(i, 5);
                            String horarioStr = horaInicio.toString() + " - " + horaFin.toString();
                            if ("Estudiante".equals(tipoFila) && fecha.toString().equals(fechaFila) && horarioStr.equals(horarioFila)) {
                                JOptionPane.showMessageDialog(null, "Ya existe un estudiante con ese horario en ese día.");
                                return;
                            }
                        }
                    }

                    // Validar si la persona puede realizar la guardia
                    if (!planificador.puedeRealizarGuardia(persona, horario)) {
                        JOptionPane.showMessageDialog(null, "La persona seleccionada no puede realizar la guardia en ese horario.");
                        return;
                    }

                    planificador.crearGuardia(idx, horario, persona);
                    persona.setCantidadGuardias(persona.getCantidadGuardias() + 1);

                    // Añadir a la tabla de guardias asignadas
                    String tipo = (persona instanceof logica.Trabajador) ? "Trabajador" : "Estudiante";
                    String horarioStr = horaInicio.toString() + " - " + horaFin.toString();
                    String generoStr = "";
                    if (persona instanceof logica.Estudiante) {
                        generoStr = ((logica.Estudiante) persona).getSexo().toString();
                    } else if (persona instanceof logica.Trabajador) {
                        generoStr = ((logica.Trabajador) persona).getSexo().toString();
                    }
                    modeloTablaGuardias.addRow(new Object[]{
                        persona.getNombre(),
                        persona.getCi(),
                        tipo,
                        generoStr,
                        fecha.toString(),
                        horarioStr
                    });

                    // Ordenar la tabla por fecha y hora
                    ordenarTablaPorFechaYHora();

                    JOptionPane.showMessageDialog(null, "Guardia asignada correctamente.");
                    actualizarPersonasFiltradas();
                    mostrarHorarioSugerido();
                    actualizarTurnoTrabajador();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error al asignar guardia: " + ex.getMessage());
                }
            }
        });
    }

    // Método para ordenar la tabla por fecha y hora
    private void ordenarTablaPorFechaYHora() {
        ArrayList<Object[]> filas = new ArrayList<Object[]>();
        for (int i = 0; i < modeloTablaGuardias.getRowCount(); i++) {
            Object[] fila = new Object[modeloTablaGuardias.getColumnCount()];
            for (int j = 0; j < modeloTablaGuardias.getColumnCount(); j++) {
                fila[j] = modeloTablaGuardias.getValueAt(i, j);
            }
            filas.add(fila);
        }
        java.util.Collections.sort(filas, new java.util.Comparator<Object[]>() {
            public int compare(Object[] o1, Object[] o2) {
                try {
                    java.time.LocalDate fecha1 = java.time.LocalDate.parse(o1[4].toString());
                    java.time.LocalDate fecha2 = java.time.LocalDate.parse(o2[4].toString());
                    int cmp = fecha1.compareTo(fecha2);
                    if (cmp != 0) return cmp;
                    // Extraer hora de inicio
                    String[] partes1 = o1[5].toString().split(" - ");
                    String[] partes2 = o2[5].toString().split(" - ");
                    java.time.LocalTime hora1 = java.time.LocalTime.parse(partes1[0]);
                    java.time.LocalTime hora2 = java.time.LocalTime.parse(partes2[0]);
                    return hora1.compareTo(hora2);
                } catch (Exception ex) {
                    return 0;
                }
            }
        });
        modeloTablaGuardias.setRowCount(0);
        for (Object[] fila : filas) {
            modeloTablaGuardias.addRow(fila);
        }
    }

    private void mostrarHorarioSugerido() {
        int idx = cbPersonas.getSelectedIndex();
        java.util.Date fechaDate = dateChooser.getDate();
        if (idx == -1 || fechaDate == null) {
            lblHorarioSugerido.setText("Horario sugerido: ");
            return;
        }
        Persona persona = personasFiltradas.get(idx);
        java.time.LocalDate fecha = fechaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        java.time.DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        utiles.Dia dia = utiles.Dia.valueOf(dayOfWeek.name());

        String texto = "No disponible";
        if (persona instanceof logica.Trabajador) {
            if (dia == utiles.Dia.SATURDAY || dia == utiles.Dia.SUNDAY) {
                String turno = (String) cbTurnoTrabajador.getSelectedItem();
                texto = (turno != null) ? turno : "9:00 - 14:00 o 14:00 - 19:00";
            }
        } else if (persona instanceof logica.Estudiante) {
            if (((logica.Estudiante) persona).getSexo() == utiles.Sexo.MASCULINO) {
                texto = "20:00 - 08:00 (todos los días)";
            } else {
                if (dia == utiles.Dia.SATURDAY || dia == utiles.Dia.SUNDAY) {
                    texto = "8:00 - 20:00";
                }
            }
        }
        lblHorarioSugerido.setText("Horario sugerido: " + texto);
    }

    private void actualizarTurnoTrabajador() {
        int idx = cbPersonas.getSelectedIndex();
        java.util.Date fechaDate = dateChooser.getDate();
        if (idx == -1 || fechaDate == null) {
            cbTurnoTrabajador.setEnabled(false);
            return;
        }
        Persona persona = personasFiltradas.get(idx);
        java.time.LocalDate fecha = fechaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        java.time.DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        utiles.Dia dia = utiles.Dia.valueOf(dayOfWeek.name());

        if (persona instanceof logica.Trabajador && (dia == utiles.Dia.SATURDAY || dia == utiles.Dia.SUNDAY)) {
            cbTurnoTrabajador.setEnabled(true);
        } else {
            cbTurnoTrabajador.setEnabled(false);
        }
    }
}
