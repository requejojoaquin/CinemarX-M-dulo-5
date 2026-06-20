package CINEMARX.M5;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import CINEMARX.Common.OrderDetails;
import CINEMARX.Common.Boleto;
import javax.imageio.ImageIO;
import java.net.URL;

public class ReciboViewer extends JDialog {

    private static final Color COLOR_FONDO = new Color(245, 245, 245);
    private static final Color COLOR_TEXTO = new Color(30, 30, 30);
    private static final Color COLOR_HEADER = new Color(239, 68, 68);

    private JPanel reciboPanel;
    private int idComprobante;
    private OrderDetails order;
    private Cliente cliente;
    private String numComprobante;

    public ReciboViewer(Frame owner, int idComprobante, OrderDetails order, Cliente cliente) {
        super(owner, "Recibo de Compra", true);
        this.idComprobante = idComprobante;
        this.order = order;
        this.cliente = cliente;
        
        fetchReciboData();
        initUI();
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        reciboPanel = new JPanel();
        reciboPanel.setLayout(new BoxLayout(reciboPanel, BoxLayout.Y_AXIS));
        reciboPanel.setBackground(Color.WHITE);
        reciboPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- Header with Logo ---
        try {
            URL imageUrl = new URL("https://gaelschenone.aguilucho.ar/source_cmx/index.php?preview=logos%2FCINEMARX%20imagotipo.png");
            BufferedImage originalImage = ImageIO.read(imageUrl);
            if (originalImage != null) {
                int newHeight = 40;
                int newWidth = (originalImage.getWidth() * newHeight) / originalImage.getHeight();
                Image logo = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(logo));
                logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                reciboPanel.add(logoLabel);
                reciboPanel.add(Box.createVerticalStrut(15)); // Space after logo
            }
        } catch (IOException e) {
            System.err.println("Error loading logo image: " + e.getMessage());
        }

        // --- Receipt Details ---
        JLabel lblTitulo = new JLabel("RECIBO DE COMPRA");
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        reciboPanel.add(lblTitulo);
        reciboPanel.add(Box.createVerticalStrut(10));


        // Details
        JEditorPane detailsPane = new JEditorPane();
        detailsPane.setContentType("text/html");
        detailsPane.setEditable(false);
        detailsPane.setBackground(COLOR_FONDO);
        detailsPane.setText(buildHtmlRecibo());
        
        JScrollPane scrollPane = new JScrollPane(detailsPane);
        scrollPane.setBorder(null);
        reciboPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer (Download Button)
        JButton btnDescargar = new JButton("Descargar como PNG");
        btnDescargar.setFont(new Font("Arial", Font.BOLD, 14));
        btnDescargar.setBackground(COLOR_HEADER);
        btnDescargar.setForeground(Color.WHITE);
        btnDescargar.setFocusPainted(false);
        btnDescargar.addActionListener((ActionEvent e) -> descargarRecibo());
        
        reciboPanel.add(btnDescargar, BorderLayout.SOUTH);

        add(reciboPanel);
    }

    private String buildHtmlRecibo() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Arial, sans-serif; color: #333; margin: 10px;'>");
        
        sb.append("<h2>Gracias por tu compra, ").append(cliente.getNombre()).append("!</h2>");
        sb.append("<p><strong>Número de Comprobante:</strong> ").append(numComprobante).append("</p>");
        sb.append("<hr>");

        sb.append("<h3>Boletos</h3>");
        sb.append("<ul>");
        for (Boleto boleto : order.getBoletos()) {
            sb.append("<li>Butaca ").append(boleto.getNumeroButaca()).append(" - Precio: ").append(formatearPrecio(getPrecioFuncion(boleto.getIdFuncion()))).append("</li>");
        }
        sb.append("</ul>");

        if (!order.getProductItems().isEmpty()) {
            sb.append("<h3>Buffet</h3>");
            sb.append("<ul>");
            for (OrderDetails.OrderItem item : order.getProductItems()) {
                String personalizacion = (item.personalizacion != null && !item.personalizacion.isEmpty())
                    ? " (" + item.personalizacion.replace(";", ", ") + ")"
                    : "";
                sb.append("<li>").append(item.producto.getNombre()).append(personalizacion).append(" - ").append(formatearPrecio(item.producto.getPrecio())).append("</li>");
            }
            sb.append("</ul>");
        }
        
        sb.append("<hr>");
        sb.append("<div style='text-align:right; font-size: 1.2em;'>");
        sb.append("<strong>Total: ").append(formatearPrecio(calcularTotal())).append("</strong>");
        sb.append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private void fetchReciboData() {
        Connection conn = ConexionBD.obtenerConexion();
        try (PreparedStatement ps = conn.prepareStatement("SELECT NumComprobante FROM Comprobante WHERE ID_Comprobante = ?")) {
            ps.setInt(1, idComprobante);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.numComprobante = rs.getString("NumComprobante");
            } else {
                this.numComprobante = "N/A";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            this.numComprobante = "Error";
        }
    }

    private void descargarRecibo() {
        try {
            String filename = "Recibo_" + this.numComprobante + ".png";
            saveComponentAsPNG(reciboPanel, filename);
            JOptionPane.showMessageDialog(this, "Recibo guardado como " + filename + " en tu carpeta de Descargas.", "Descarga Exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar el recibo: " + ex.getMessage(), "Error de Descarga", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveComponentAsPNG(Component comp, String filename) throws IOException {
        Dimension size = comp.getSize();
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        comp.paint(g2);
        g2.dispose();

        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        File outputFile = new File(downloadsDir, filename);
        ImageIO.write(image, "png", outputFile);
    }

    private double getPrecioFuncion(int idFuncion) {
        // This is duplicated logic, but needed for the viewer
        Connection conn = ConexionBD.obtenerConexion();
        try (PreparedStatement ps = conn.prepareStatement("SELECT Precio FROM Funcion WHERE ID_Funcion = ?")) {
            ps.setInt(1, idFuncion);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("Precio");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private double calcularTotal() {
        double total = 0;
        for (Boleto boleto : order.getBoletos()) {
            total += getPrecioFuncion(boleto.getIdFuncion());
        }
        for (OrderDetails.OrderItem item : order.getProductItems()) {
            total += item.producto.getPrecio();
        }
        return total;
    }
    
    private String formatearPrecio(double precio) {
        return NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(precio);
    }
}
