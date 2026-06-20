package CINEMARX.M5;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Locale;
import CINEMARX.Common.OrderDetails;
import CINEMARX.Common.Boleto;
import CINEMARX.Common.Producto;

public class VentanaFinalizarCompra extends JPanel {
    
    private static final Color COLOR_FONDO = new Color(45, 45, 45);
    private static final Color COLOR_FONDO_CLARO = new Color(55, 55, 55);
    private static final Color COLOR_TEXTO = Color.WHITE;
    private static final Color COLOR_BOTON = new Color(239, 68, 68);
    private static final Color COLOR_BORDE = new Color(70, 70, 70);
    
    private JCheckBox checkNuevaTarjeta;
    private JPanel panelFormularioNuevaTarjeta;
    private JPanel panelResumenItems;
    private JLabel lblTotal;
    private JButton btnRealizarPedido;
    private MetodoPago metodoSeleccionado;
    private JPanel panelContainer;
    private JLabel lblMetodoSeleccionado;
    private JPopupMenu popupMetodos;
    
    private JTextField txtNumeroTarjeta;
    private JTextField txtNombreTarjeta;
    private JComboBox<String> comboEmpresaTarjeta;
    private JTextField txtCaducidad;
    private JTextField txtCVV;
    private JCheckBox checkGuardarSi;
    private JComboBox<String> comboTipoTarjeta;
    
    private Cliente clienteActual;
    private OrderDetails order;
    
    public VentanaFinalizarCompra(OrderDetails order) {
        this.order = order;
        if (!inicializarDatos()) {
            mostrarErrorInicializacion();
            return;
        }
        inicializarComponentes();
    }
    
    private void mostrarErrorInicializacion() {
        JOptionPane.showMessageDialog(this,
            "No se pudo inicializar el panel de pago.",
            "Error de Inicialización",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private boolean inicializarDatos() {
        // An order is invalid if it's null or has NEITHER tickets NOR products.
        if (order == null || (order.getBoletos().isEmpty() && order.getProductItems().isEmpty())) {
            return false;
        }
        
        // Get the client ID directly from the OrderDetails object.
        int idCliente = order.getIdCliente();
        clienteActual = Cliente.obtenerClientePorId(idCliente);
        
        if (clienteActual == null) {
            System.err.println("Error: No se pudo encontrar el cliente con ID: " + idCliente);
            return false;
        }
        
        clienteActual.cargarMetodosPago();
        return true;
    }
    
    private void inicializarComponentes() {
        setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());
        
        add(crearPanelHeader(), BorderLayout.NORTH);
        add(crearPanelContenido(), BorderLayout.CENTER);
    }
    
    private JPanel crearPanelHeader() {
        // This can be simplified or removed if the main frame already has a header
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(COLOR_FONDO);
        panelHeader.setBorder(new EmptyBorder(20, 50, 20, 50));
        panelHeader.setPreferredSize(new Dimension(0, 100));
        JLabel title = new JLabel("Finalizar Compra");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        panelHeader.add(title);
        return panelHeader;
    }
    
    private JPanel crearPanelContenido() {
        JPanel panelContenido = new JPanel(new GridBagLayout());
        panelContenido.setBackground(COLOR_FONDO);
        panelContenido.setBorder(new EmptyBorder(5, 50, 40, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // --- Left Panel (Payment Methods) ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6; // 60% of the space
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 0, 20); // Right margin
        JPanel panelMetodos = crearPanelMetodosPago();
        panelContenido.add(panelMetodos, gbc);
        
            // --- Right Panel (Summary) ---
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 0.4; // 40% of the space
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.VERTICAL; // Only fill vertically
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.insets = new Insets(0, 20, 0, 0); // Left margin
            JPanel panelResumen = crearPanelResumen();
            panelContenido.add(panelResumen, gbc);        
        return panelContenido;
    }
    
    private JPanel crearPanelMetodosPago() {
        // This method is mostly reusable from the original class
        panelContainer = new JPanel(new BorderLayout());
        panelContainer.setBackground(COLOR_FONDO);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(0, 0, 0, 35));
        
        JLabel lblTitulo = new JLabel("Finalizar Compra");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitulo.setForeground(COLOR_TEXTO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTitulo);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel lblTusMetodos = new JLabel("Tus métodos de pago");
        lblTusMetodos.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTusMetodos.setForeground(COLOR_TEXTO);
        lblTusMetodos.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTusMetodos);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        JPanel panelListaDesplegable = crearPanelListaDesplegable();
        panel.add(panelListaDesplegable);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel lblOtrosMetodos = new JLabel("Otros métodos de pago");
        lblOtrosMetodos.setFont(new Font("Arial", Font.PLAIN, 14));
        lblOtrosMetodos.setForeground(COLOR_TEXTO);
        lblOtrosMetodos.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblOtrosMetodos);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        JPanel panelNuevaTarjeta = crearPanelNuevaTarjeta();
        panel.add(panelNuevaTarjeta);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        panelFormularioNuevaTarjeta = crearPanelFormularioTarjeta();
        panelFormularioNuevaTarjeta.setVisible(false);
        panelFormularioNuevaTarjeta.setMaximumSize(new Dimension(400, 0));
        panelFormularioNuevaTarjeta.setPreferredSize(new Dimension(400, 0));
        panelFormularioNuevaTarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(panelFormularioNuevaTarjeta);
        
        panel.add(Box.createRigidArea(new Dimension(0, 50)));
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBackground(COLOR_FONDO);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(515, 600));
        
        panelContainer.add(scrollPane, BorderLayout.CENTER);
        
        return panelContainer;
    }
    
    private JPanel crearPanelListaDesplegable() {
        JPanel panelListaDesplegable = new JPanel(new BorderLayout());
        panelListaDesplegable.setMaximumSize(new Dimension(400, 43));
        panelListaDesplegable.setPreferredSize(new Dimension(400, 43));
        panelListaDesplegable.setBackground(COLOR_FONDO_CLARO);
        panelListaDesplegable.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1));
        panelListaDesplegable.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        lblMetodoSeleccionado = new JLabel("   Seleccionar método de pago");
        lblMetodoSeleccionado.setFont(new Font("Arial", Font.PLAIN, 12));
        lblMetodoSeleccionado.setForeground(new Color(150, 150, 150));
        lblMetodoSeleccionado.setOpaque(true);
        lblMetodoSeleccionado.setBackground(COLOR_FONDO_CLARO);
        
        JLabel lblFlecha = new JLabel("▼  ");
        lblFlecha.setFont(new Font("Arial", Font.PLAIN, 10));
        lblFlecha.setForeground(COLOR_TEXTO);
        lblFlecha.setOpaque(true);
        lblFlecha.setBackground(COLOR_FONDO_CLARO);
        
        panelListaDesplegable.add(lblMetodoSeleccionado, BorderLayout.CENTER);
        panelListaDesplegable.add(lblFlecha, BorderLayout.EAST);
        
        popupMetodos = new JPopupMenu();
        popupMetodos.setBackground(COLOR_FONDO_CLARO);
        popupMetodos.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1));
        
        cargarMetodosPagoEnPopup();
        
        panelListaDesplegable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                popupMetodos.show(panelListaDesplegable, 0, panelListaDesplegable.getHeight());
            }
        });
        
        panelListaDesplegable.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return panelListaDesplegable;
    }
    
    private void cargarMetodosPagoEnPopup() {
        popupMetodos.removeAll();
        
        ArrayList<MetodoPago> metodosPago = clienteActual.getMetodosPago();
        
        if (metodosPago.isEmpty()) {
            JMenuItem item = new JMenuItem("   No hay métodos de pago guardados");
            item.setFont(new Font("Arial", Font.ITALIC, 12));
            item.setForeground(new Color(150, 150, 150));
            item.setBackground(COLOR_FONDO_CLARO);
            item.setEnabled(false);
            popupMetodos.add(item);
        } else {
            for (MetodoPago metodo : metodosPago) {
                JMenuItem item = new JMenuItem("   " + metodo.toString());
                item.setFont(new Font("Arial", Font.PLAIN, 12));
                item.setForeground(COLOR_TEXTO);
                item.setBackground(COLOR_FONDO_CLARO);
                item.setOpaque(true);
                item.setBorder(new EmptyBorder(10, 10, 10, 10));
                
                item.addActionListener(e -> {
                    lblMetodoSeleccionado.setText("   " + metodo.toString());
                    lblMetodoSeleccionado.setForeground(COLOR_TEXTO);
                    metodoSeleccionado = metodo;
                    
                    checkNuevaTarjeta.setSelected(false);
                    panelFormularioNuevaTarjeta.setVisible(false);
                    panelFormularioNuevaTarjeta.setMaximumSize(new Dimension(400, 0));
                    panelFormularioNuevaTarjeta.setPreferredSize(new Dimension(400, 0));
                    limpiarFormulario();
                    panelContainer.revalidate();
                    panelContainer.repaint();
                });
                
                popupMetodos.add(item);
            }
        }
    }
    
    private JPanel crearPanelNuevaTarjeta() {
        JPanel panelNuevaTarjeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 13));
        panelNuevaTarjeta.setBackground(COLOR_FONDO_CLARO);
        panelNuevaTarjeta.setMaximumSize(new Dimension(400, 51));
        panelNuevaTarjeta.setPreferredSize(new Dimension(400, 51));
        panelNuevaTarjeta.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1, true));
        panelNuevaTarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        checkNuevaTarjeta = new JCheckBox();
        checkNuevaTarjeta.setBackground(COLOR_FONDO_CLARO);
        checkNuevaTarjeta.setForeground(COLOR_TEXTO);
        checkNuevaTarjeta.setFocusPainted(false);

        JLabel lblIconoTarjeta = new JLabel("💳");
        lblIconoTarjeta.setFont(new Font("Arial", Font.PLAIN, 20));

        JLabel lblNuevaTarjeta = new JLabel("Tarjeta de crédito o débito");
        lblNuevaTarjeta.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNuevaTarjeta.setForeground(COLOR_TEXTO);

        panelNuevaTarjeta.add(checkNuevaTarjeta);
        panelNuevaTarjeta.add(lblIconoTarjeta);
        panelNuevaTarjeta.add(lblNuevaTarjeta);
        
        checkNuevaTarjeta.addActionListener(e -> {
            boolean mostrar = checkNuevaTarjeta.isSelected();
            
            if (mostrar) {
                lblMetodoSeleccionado.setText("   Seleccionar método de pago");
                lblMetodoSeleccionado.setForeground(new Color(150, 150, 150));
                metodoSeleccionado = null;
                
                panelFormularioNuevaTarjeta.setVisible(true);
                panelFormularioNuevaTarjeta.setMaximumSize(new Dimension(400, 480));
                panelFormularioNuevaTarjeta.setPreferredSize(new Dimension(400, 480));
            } else {
                panelFormularioNuevaTarjeta.setVisible(false);
                panelFormularioNuevaTarjeta.setMaximumSize(new Dimension(400, 0));
                panelFormularioNuevaTarjeta.setPreferredSize(new Dimension(400, 0));
                limpiarFormulario();
            }
            
            panelContainer.revalidate();
            panelContainer.repaint();
        });
        
        return panelNuevaTarjeta;
    }
    
    private JPanel crearPanelFormularioTarjeta() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO_CLARO);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
            new EmptyBorder(17, 17, 17, 17)
        ));
        panel.setMaximumSize(new Dimension(400, 480));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        agregarCampoFormulario(panel, "Número de la tarjeta*", 
            txtNumeroTarjeta = crearCampoTexto());
        
        agregarCampoFormulario(panel, "Nombre en la tarjeta*", 
            txtNombreTarjeta = crearCampoTexto());
        
        JLabel lblEmpresa = new JLabel("Empresa de tarjeta*");
        lblEmpresa.setFont(new Font("Arial", Font.PLAIN, 10));
        lblEmpresa.setForeground(new Color(180, 180, 180));
        lblEmpresa.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblEmpresa);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        
        String[] empresas = {"Mastercard", "Visa", "Patagonia365", "American Express"};
        comboEmpresaTarjeta = new JComboBox<>(empresas);
        comboEmpresaTarjeta.setFont(new Font("Arial", Font.PLAIN, 13));
        comboEmpresaTarjeta.setBackground(Color.WHITE);
        comboEmpresaTarjeta.setForeground(Color.BLACK);
        comboEmpresaTarjeta.setMaximumSize(new Dimension(366, 36));
        comboEmpresaTarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(comboEmpresaTarjeta);
        panel.add(Box.createRigidArea(new Dimension(0, 13)));
        
        JLabel lblTipo = new JLabel("Tipo de tarjeta*");
        lblTipo.setFont(new Font("Arial", Font.PLAIN, 10));
        lblTipo.setForeground(new Color(180, 180, 180));
        lblTipo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTipo);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        
        String[] tipos = {"Credito", "Debito"};
        comboTipoTarjeta = new JComboBox<>(tipos);
        comboTipoTarjeta.setFont(new Font("Arial", Font.PLAIN, 13));
        comboTipoTarjeta.setBackground(Color.WHITE);
        comboTipoTarjeta.setForeground(Color.BLACK);
        comboTipoTarjeta.setMaximumSize(new Dimension(366, 36));
        comboTipoTarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(comboTipoTarjeta);
        panel.add(Box.createRigidArea(new Dimension(0, 13)));
        
        agregarCampoFormulario(panel, "Caducidad (MM/AAAA)*", 
            txtCaducidad = crearCampoTexto());
        
        agregarCampoFormulario(panel, "CVV*", 
            txtCVV = crearCampoTexto());
        
        JPanel panelGuardarContainer = new JPanel();
        panelGuardarContainer.setLayout(new BoxLayout(panelGuardarContainer, BoxLayout.Y_AXIS));
        panelGuardarContainer.setBackground(COLOR_FONDO_CLARO);
        panelGuardarContainer.setMaximumSize(new Dimension(366, 80));
        panelGuardarContainer.setPreferredSize(new Dimension(366, 80));
        panelGuardarContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblGuardar = new JLabel("¿Quieres guardar este método de pago para futuras compras?");
        lblGuardar.setFont(new Font("Arial", Font.PLAIN, 10));
        lblGuardar.setForeground(COLOR_TEXTO);
        lblGuardar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelGuardarContainer.add(lblGuardar);
        panelGuardarContainer.add(Box.createRigidArea(new Dimension(0, 12)));
        
        JPanel panelOpciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelOpciones.setBackground(COLOR_FONDO_CLARO);
        panelOpciones.setMaximumSize(new Dimension(366, 30));
        panelOpciones.setPreferredSize(new Dimension(366, 30));
        panelOpciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        checkGuardarSi = new JCheckBox("SI");
        checkGuardarSi.setBackground(COLOR_FONDO_CLARO);
        checkGuardarSi.setForeground(COLOR_TEXTO);
        checkGuardarSi.setFont(new Font("Arial", Font.PLAIN, 12));
        checkGuardarSi.setFocusPainted(false);
        
        panelOpciones.add(checkGuardarSi);
        panelGuardarContainer.add(panelOpciones);
        panel.add(panelGuardarContainer);
        
        return panel;
    }
    
    private JTextField crearCampoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Arial", Font.PLAIN, 13));
        campo.setForeground(Color.BLACK);
        campo.setBackground(Color.WHITE);
        campo.setCaretColor(Color.BLACK);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        campo.setMaximumSize(new Dimension(366, 36));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        return campo;
    }
    
    private void agregarCampoFormulario(JPanel panel, String etiqueta, JTextField campo) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Arial", Font.PLAIN, 10));
        lbl.setForeground(new Color(180, 180, 180));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(campo);
        panel.add(Box.createRigidArea(new Dimension(0, 13)));
    }
    
    private void limpiarFormulario() {
        txtNumeroTarjeta.setText("");
        txtNombreTarjeta.setText("");
        txtCaducidad.setText("");
        txtCVV.setText("");
        checkGuardarSi.setSelected(false);
        comboTipoTarjeta.setSelectedIndex(0);
        comboEmpresaTarjeta.setSelectedIndex(0);
    }
    
    private JPanel crearPanelResumen() {
        JPanel panelWrapper = new JPanel();
        panelWrapper.setLayout(new BoxLayout(panelWrapper, BoxLayout.Y_AXIS));
        panelWrapper.setBackground(COLOR_FONDO);
        panelWrapper.setBorder(new EmptyBorder(0, 60, 0, 0));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO);
        
        JLabel lblTitulo = new JLabel("Resumen de tu compra");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(COLOR_TEXTO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTitulo);
        panel.add(Box.createRigidArea(new Dimension(0, 29)));
        
        panelResumenItems = new JPanel();
        panelResumenItems.setLayout(new BoxLayout(panelResumenItems, BoxLayout.Y_AXIS));
        panelResumenItems.setBackground(COLOR_FONDO);
        panelResumenItems.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add boletos to summary
        for (Boleto boleto : order.getBoletos()) {
            double precioBoleto = getPrecioFuncion(boleto.getIdFuncion());
            agregarItemResumen("Boleto Butaca " + boleto.getNumeroButaca(), precioBoleto);
        }

        // Add productos to summary
        for (OrderDetails.OrderItem item : order.getProductItems()) {
            String description = item.producto.getNombre();
            if (item.personalizacion != null && !item.personalizacion.isEmpty()) {
                description += " (" + item.personalizacion.replace(";", ", ") + ")";
            }
            agregarItemResumen(description, item.producto.getPrecio());
        }
        
        panel.add(panelResumenItems);
        panel.add(Box.createRigidArea(new Dimension(0, 29)));
        
        JSeparator separador = new JSeparator();
        separador.setForeground(COLOR_BORDE);
        separador.setBackground(COLOR_BORDE);
        separador.setMaximumSize(new Dimension(440, 1));
        separador.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(separador);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel panelTotal = new JPanel(new BorderLayout(9, 0));
        panelTotal.setBackground(COLOR_FONDO);
        panelTotal.setMaximumSize(new Dimension(440, 29));
        panelTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblTotalTexto = new JLabel("Total");
        lblTotalTexto.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotalTexto.setForeground(COLOR_TEXTO);
        
        lblTotal = new JLabel(formatearPrecio(calcularTotal()));
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(COLOR_TEXTO);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        
        panelTotal.add(lblTotalTexto, BorderLayout.WEST);
        panelTotal.add(lblTotal, BorderLayout.EAST);
        
        panel.add(panelTotal);
        panel.add(Box.createRigidArea(new Dimension(0, 49)));
        
        btnRealizarPedido = new JButton("Realizar pedido");
        btnRealizarPedido.setFont(new Font("Arial", Font.BOLD, 15));
        btnRealizarPedido.setForeground(COLOR_FONDO);
        btnRealizarPedido.setBackground(COLOR_BOTON);
        btnRealizarPedido.setFocusPainted(false);
        btnRealizarPedido.setBorderPainted(false);
        btnRealizarPedido.setMaximumSize(new Dimension(440, 49));
        btnRealizarPedido.setPreferredSize(new Dimension(440, 49));
        btnRealizarPedido.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRealizarPedido.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRealizarPedido.setOpaque(true);
        
        btnRealizarPedido.addActionListener(e -> realizarPedido());
        
        panel.add(btnRealizarPedido);
        
        panelWrapper.add(panel);
        
        return panelWrapper;
    }

    private void agregarItemResumen(String descripcion, double precio) {
        JPanel panelLinea = new JPanel(new BorderLayout(9, 0));
        panelLinea.setBackground(COLOR_FONDO);
        panelLinea.setMaximumSize(new Dimension(440, 29));
        panelLinea.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblResumenCompra = new JLabel(descripcion);
        lblResumenCompra.setFont(new Font("Arial", Font.PLAIN, 14));
        lblResumenCompra.setForeground(COLOR_TEXTO);
        
        JLabel lblPrecio = new JLabel(formatearPrecio(precio));
        lblPrecio.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPrecio.setForeground(COLOR_TEXTO);
        lblPrecio.setHorizontalAlignment(SwingConstants.RIGHT);
        
        panelLinea.add(lblResumenCompra, BorderLayout.WEST);
        panelLinea.add(lblPrecio, BorderLayout.EAST);
        
        panelResumenItems.add(panelLinea);
    }

    private double getPrecioFuncion(int idFuncion) {
        Connection conn = ConexionBD.obtenerConexion();
        try {
            String sql = "SELECT Precio FROM Funcion WHERE ID_Funcion = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idFuncion);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("Precio");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    private void realizarPedido() {
        if (metodoSeleccionado == null && !checkNuevaTarjeta.isSelected()) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un método de pago.", "Error de Pago", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MetodoPago metodoFinal = metodoSeleccionado;

        // --- Lógica para manejar una nueva tarjeta ---
        if (checkNuevaTarjeta.isSelected()) {
            String numero = txtNumeroTarjeta.getText();
            String nombre = txtNombreTarjeta.getText();
            String caducidad = txtCaducidad.getText();
            String cvv = txtCVV.getText();

            if (numero.isEmpty() || nombre.isEmpty() || caducidad.isEmpty() || cvv.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, completa todos los campos de la nueva tarjeta.", "Datos Incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            metodoFinal = new MetodoPago(
                (String) comboEmpresaTarjeta.getSelectedItem(),
                (String) comboTipoTarjeta.getSelectedItem(),
                numero.length() > 4 ? numero.substring(numero.length() - 4) : numero,
                cvv
            );
            metodoFinal.setNombreTitular(nombre);
            metodoFinal.setFechaCaducidad(caducidad);
            metodoFinal.setNumeroCompleto(numero);

            if (checkGuardarSi.isSelected()) {
                try {
                    if (!clienteActual.agregarMetodoPago(metodoFinal)) {
                         JOptionPane.showMessageDialog(this, "No se pudo guardar el nuevo método de pago.", "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (SQLException e) {
                    if (e.getSQLState().equals("23000")) { // Código de violación de integridad (duplicado)
                        JOptionPane.showMessageDialog(this, "El número de tarjeta ya se encuentra registrado.", "Tarjeta Duplicada", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al guardar el método de pago: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }
            }
        }

        // --- Lógica para guardar el pedido en la base de datos usando la estructura de Comprobante ---
        Connection conn = null;
        try {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false);

            // 1. Crear Comprobante para obtener su ID
            int idComprobante = -1;
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            int randomNum = new Random().nextInt(9000) + 1000; // 4-digit random number
            String numComprobante = "COMP-" + timestamp + "-" + randomNum;
            
            String sqlComprobante = "INSERT INTO Comprobante (NumComprobante, ID_Cliente, FechaCompra, MetodoPago) VALUES (?, ?, NOW(), ?)";
            try (PreparedStatement psComprobante = conn.prepareStatement(sqlComprobante, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psComprobante.setString(1, numComprobante);
                psComprobante.setInt(2, clienteActual.getIdCliente());
                psComprobante.setString(3, metodoFinal.getEmpresa() + " " + metodoFinal.getTipo());
                psComprobante.executeUpdate();

                ResultSet rs = psComprobante.getGeneratedKeys();
                if (rs.next()) {
                    idComprobante = rs.getInt(1);
                }
            }

            if (idComprobante == -1) {
                throw new SQLException("No se pudo crear el comprobante.");
            }

            // 2. Guardar Boletos y luego asociarlos en Comprobante_Boleto
            String sqlBoleto = "INSERT INTO Boleto (NumeroButaca, ID_Funcion, ID_Cliente) VALUES (?, ?, ?)";
            String sqlComprobanteBoleto = "INSERT INTO Comprobante_Boleto (ID_Comprobante, ID_Boleto) VALUES (?, ?)";
            
            for (Boleto boleto : order.getBoletos()) {
                int idBoleto = -1;
                try (PreparedStatement psBoleto = conn.prepareStatement(sqlBoleto, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psBoleto.setString(1, boleto.getNumeroButaca());
                    psBoleto.setInt(2, boleto.getIdFuncion());
                    psBoleto.setInt(3, boleto.getIdCliente());
                    psBoleto.executeUpdate();
                    
                    ResultSet rs = psBoleto.getGeneratedKeys();
                    if (rs.next()) {
                        idBoleto = rs.getInt(1);
                    }
                }
                if (idBoleto == -1) {
                    throw new SQLException("No se pudo guardar el boleto para la butaca " + boleto.getNumeroButaca());
                }

                try (PreparedStatement psCompBoleto = conn.prepareStatement(sqlComprobanteBoleto)) {
                    psCompBoleto.setInt(1, idComprobante);
                    psCompBoleto.setInt(2, idBoleto);
                    psCompBoleto.executeUpdate();
                }
            }

            // 3. Guardar Productos en Comprobante_Producto
            if (!order.getProductItems().isEmpty()) {
                String sqlProducto = "INSERT INTO Comprobante_Producto (ID_Comprobante, ID_Prod, Cantidad, Extra) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psProducto = conn.prepareStatement(sqlProducto)) {
                    // Agrupar productos por ID, acumulando cantidad y extras
                    Map<Integer, Map.Entry<Integer, List<String>>> productosAgrupados = new HashMap<>();
                    for (OrderDetails.OrderItem item : order.getProductItems()) {
                        int prodId = item.producto.getId();
                        
                        Map.Entry<Integer, List<String>> entry = productosAgrupados.getOrDefault(prodId, new AbstractMap.SimpleEntry<>(0, new ArrayList<>()));
                        
                        int newQty = entry.getKey() + 1;
                        List<String> extras = entry.getValue();
                        if (item.personalizacion != null && !item.personalizacion.isEmpty()) {
                            extras.add(item.personalizacion);
                        }
                        
                        productosAgrupados.put(prodId, new AbstractMap.SimpleEntry<>(newQty, extras));
                    }

                    for (Map.Entry<Integer, Map.Entry<Integer, List<String>>> finalEntry : productosAgrupados.entrySet()) {
                        int prodId = finalEntry.getKey();
                        int cantidad = finalEntry.getValue().getKey();
                        String extras = String.join("; ", finalEntry.getValue().getValue());

                        psProducto.setInt(1, idComprobante);
                        psProducto.setInt(2, prodId);
                        psProducto.setInt(3, cantidad);
                        psProducto.setString(4, extras);
                        psProducto.addBatch();
                    }
                    psProducto.executeBatch();
                }
            }

            conn.commit();

            // --- Activar membresía si se compró ---
            boolean membresiaComprada = false;
            for (OrderDetails.OrderItem item : order.getProductItems()) {
                if (item.producto.getId() == 20) {
                    Cliente.activarMembresia(clienteActual.getIdCliente());
                    MembresiaActivadaDialog dialog = new MembresiaActivadaDialog((Frame) SwingUtilities.getWindowAncestor(this));
                    dialog.setVisible(true);
                    membresiaComprada = true;
                    break;
                }
            }

            // --- Sumar puntos si el cliente es VIP (y no es la compra de la membresía) ---
            if (!membresiaComprada && clienteActual.esVIP()) {
                double totalCompra = calcularTotal();
                int puntosGanados = (int) (totalCompra / 100);
                if (puntosGanados > 0) {
                    int puntosActuales = clienteActual.getPuntos();
                    int puntosTotales = puntosActuales + puntosGanados;

                    System.out.println("--- DEBUG PUNTOS VIP ---");
                    System.out.println("Puntos Actuales: " + puntosActuales);
                    System.out.println("Puntos Ganados: " + puntosGanados);
                    System.out.println("Puntos Totales: " + puntosTotales);
                    System.out.println("------------------------");

                    clienteActual.sumarPuntos(puntosGanados);
                    JOptionPane.showMessageDialog(this, "¡Felicidades! Has ganado " + puntosGanados + " puntos.", "Puntos VIP", JOptionPane.INFORMATION_MESSAGE);
                }
            }

            // --- Mostrar el Recibo ---
            final int finalIdComprobante = idComprobante;
            SwingUtilities.invokeLater(() -> {
                ReciboViewer reciboViewer = new ReciboViewer((Frame) SwingUtilities.getWindowAncestor(this), finalIdComprobante, order, clienteActual);
                reciboViewer.setVisible(true);
                
                // Opcional: Limpiar el panel de compra o navegar a otro lado
                removeAll();
                // Por ejemplo, podrías mostrar un panel de agradecimiento simple
                setLayout(new BorderLayout());
                JLabel lblGracias = new JLabel("¡Gracias por tu compra!", SwingConstants.CENTER);
                lblGracias.setFont(new Font("Arial", Font.BOLD, 24));
                lblGracias.setForeground(Color.WHITE);
                add(lblGracias, BorderLayout.CENTER);
                revalidate();
                repaint();
            });

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    System.err.println("Error en la transacción, realizando rollback.");
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error al guardar el pedido en la base de datos: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
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
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        return formato.format(precio);
    }
}