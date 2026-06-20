package CINEMARX.M5;

import javax.swing.*;
import java.awt.*;

public class MembresiaActivadaDialog extends JDialog {

    public MembresiaActivadaDialog(Frame owner) {
        super(owner, "Membresía Activada", true);
        initComponentes();
    }

    private void initComponentes() {
        setSize(400, 300);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        getContentPane().setBackground(new Color(30, 30, 30));
        setLayout(new GridBagLayout());

        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBackground(new Color(30, 30, 30));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblIcono = new JLabel("✓");
        lblIcono.setFont(new Font("Segoe UI", Font.BOLD, 80));
        lblIcono.setForeground(new Color(76, 175, 80)); // Green color
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("¡Felicidades!");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMensaje = new JLabel("Tu membresía VIP ha sido activada.");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblMensaje.setForeground(new Color(180, 180, 180));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setPreferredSize(new Dimension(120, 40));
        btnAceptar.setMaximumSize(new Dimension(120, 40));
        btnAceptar.setBackground(new Color(76, 175, 80));
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorderPainted(false);
        btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAceptar.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnAceptar.addActionListener(e -> dispose());

        panelCentral.add(lblIcono);
        panelCentral.add(Box.createVerticalStrut(15));
        panelCentral.add(lblTitulo);
        panelCentral.add(Box.createVerticalStrut(10));
        panelCentral.add(lblMensaje);
        panelCentral.add(Box.createVerticalStrut(25));
        panelCentral.add(btnAceptar);

        add(panelCentral);
    }
}
