package CINEMARX.M5;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Clase que representa un Cliente del sistema
 * Basada en el diagrama de clases
 */
public class Cliente {
    private int idCliente;
    private int dni;
    private String nombre;
    private String apellido;
    private String fechaNac;
    private ArrayList<MetodoPago> metodosPago;
    
    public Cliente() {
        this.metodosPago = new ArrayList<>();
    }
    
    public Cliente(int idCliente, int dni, String nombre, String apellido, String fechaNac) {
        this.idCliente = idCliente;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNac = fechaNac;
        this.metodosPago = new ArrayList<>();
    }
    
    // Getters y Setters
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    
    public int getDni() { return dni; }
    public void setDni(int dni) { this.dni = dni; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    
    public String getFechaNac() { return fechaNac; }
    public void setFechaNac(String fechaNac) { this.fechaNac = fechaNac; }
    
    public ArrayList<MetodoPago> getMetodosPago() { return metodosPago; }
    public void setMetodosPago(ArrayList<MetodoPago> metodosPago) { this.metodosPago = metodosPago; }
    
    /**
     * Obtiene un cliente aleatorio de la base de datos
     */
    public static Cliente obtenerClienteAleatorio() {
        Cliente cliente = null;
        Connection conn = ConexionBD.obtenerConexion();
        
        String query = "SELECT c.ID_Cliente, u.DNI, u.Nombre, u.Apellido, u.FechaNac " +
                      "FROM Cliente c " +
                      "INNER JOIN Usuario u ON c.DNI = u.DNI " +
                      "ORDER BY RAND() LIMIT 1";
        
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                cliente = new Cliente(
                    rs.getInt("ID_Cliente"),
                    rs.getInt("DNI"),
                    rs.getString("Nombre"),
                    rs.getString("Apellido"),
                    rs.getString("FechaNac")
                );
                
                System.out.println("\n╔═══════════════════════════════════╗");
                System.out.println("  USUARIO SELECCIONADO");
                System.out.println("╚═══════════════════════════════════╝");
                System.out.println("  ID Cliente: " + cliente.getIdCliente());
                System.out.println("  Nombre: " + cliente.getNombre() + " " + cliente.getApellido());
                System.out.println("  DNI: " + cliente.getDni());
                System.out.println("╚═══════════════════════════════════╝\n");
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error al obtener cliente aleatorio");
            e.printStackTrace();
        }
        
        return cliente;
    }
    
    public static Cliente obtenerClientePorId(int idCliente) {
        Cliente cliente = null;
        Connection conn = ConexionBD.obtenerConexion();
        
        String query = "SELECT c.ID_Cliente, u.DNI, u.Nombre, u.Apellido, u.FechaNac " +
                      "FROM Cliente c " +
                      "INNER JOIN Usuario u ON c.DNI = u.DNI " +
                      "WHERE c.ID_Cliente = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idCliente);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                cliente = new Cliente(
                    rs.getInt("ID_Cliente"),
                    rs.getInt("DNI"),
                    rs.getString("Nombre"),
                    rs.getString("Apellido"),
                    rs.getString("FechaNac")
                );
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return cliente;
    }

    /**
     * Carga los métodos de pago asociados a este cliente
     */
    public void cargarMetodosPago() {
        this.metodosPago.clear();
        Connection conn = ConexionBD.obtenerConexion();
        
        String query = "SELECT * FROM MetodosPago WHERE ID_Cliente = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.idCliente);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                MetodoPago metodo = new MetodoPago(
                    rs.getString("Empresa"),
                    rs.getString("Tipo"),
                    rs.getString("Numero").substring(rs.getString("Numero").length() - 4),
                    rs.getString("Pin")
                );
                metodo.setIdMetodo(rs.getInt("ID_Metodo"));
                metodo.setNombreTitular(rs.getString("NombreTitular"));
                metodo.setFechaCaducidad(rs.getString("FechaCaducidad"));
                metodo.setNumeroCompleto(rs.getString("Numero"));
                
                this.metodosPago.add(metodo);
            }
            
            System.out.println("✓ Métodos de pago cargados: " + this.metodosPago.size());
            
        } catch (SQLException e) {
            System.err.println("✗ Error al cargar métodos de pago");
            e.printStackTrace();
        }
    }
    
    /**
     * Agrega un nuevo método de pago a este cliente
     * MODIFICADO: Ya no determina automáticamente la empresa, usa la que viene en el objeto MetodoPago
     */
    public boolean agregarMetodoPago(MetodoPago metodo) throws SQLException {
        Connection conn = ConexionBD.obtenerConexion();
        
        String query = "INSERT INTO MetodosPago (Empresa, Tipo, Numero, Pin, NombreTitular, FechaCaducidad, ID_Cliente) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            String empresa = metodo.getEmpresa();
            
            stmt.setString(1, empresa);
            stmt.setString(2, metodo.getTipo());
            stmt.setString(3, metodo.getNumeroCompleto());
            stmt.setString(4, "0000"); // PIN por defecto
            stmt.setString(5, metodo.getNombreTitular());
            stmt.setString(6, metodo.getFechaCaducidad());
            stmt.setInt(7, this.idCliente);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    metodo.setIdMetodo(rs.getInt(1));
                }
                
                this.metodosPago.add(metodo);
                return true;
            }
            
        } catch (SQLException e) {
            // Re-throw the exception to be handled by the caller
            throw e;
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return nombre + " " + apellido + " (DNI: " + dni + ")";
    }

    public boolean esVIP() {
        Connection conn = ConexionBD.obtenerConexion();
        String query = "SELECT Membresia FROM Cliente WHERE ID_Cliente = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.idCliente);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return "VIP".equalsIgnoreCase(rs.getString("Membresia"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    public void sumarPuntos(int puntos) {
        Connection conn = ConexionBD.obtenerConexion();
        String query = "UPDATE Cliente SET Puntos = Puntos + ? WHERE ID_Cliente = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, puntos);
            stmt.setInt(2, this.idCliente);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ " + puntos + " puntos sumados al cliente " + this.idCliente);
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error al sumar puntos al cliente");
            e.printStackTrace();
        }
    }

    public int getPuntos() {
        Connection conn = ConexionBD.obtenerConexion();
        String query = "SELECT Puntos FROM Cliente WHERE ID_Cliente = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.idCliente);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("Puntos");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    public static void activarMembresia(int idCliente) {
        Connection conn = ConexionBD.obtenerConexion();
        String query = "UPDATE Cliente SET Membresia = 'VIP' WHERE ID_Cliente = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idCliente);
            stmt.executeUpdate();
            System.out.println("✓ Membresía VIP activada para el cliente " + idCliente);
        } catch (SQLException e) {
            System.err.println("✗ Error al activar la membresía VIP.");
            e.printStackTrace();
        }
    }
}