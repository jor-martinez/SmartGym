package mx.infornet.smartgym;

public class Rutinas {
    private int id;
    private String nombre;
    private String descripcion;
    private int idCoach;

    public Rutinas(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
