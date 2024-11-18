import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Cliente {
    private static final String HOST = "localhost";
    private static final int PUERTO = 12345;
    private static JFrame ventana;
    private static JLabel estadoLabel;
    private static JButton boton;
    private static boolean juegoTerminado = false;

    public static void main(String[] args) {
        ventana = new JFrame("Cliente");
        estadoLabel = new JLabel("Continúa jugando", SwingConstants.CENTER);
        boton = new JButton("Avanzar");

        ventana.setLayout(new BorderLayout());
        ventana.add(estadoLabel, BorderLayout.NORTH);
        ventana.add(boton, BorderLayout.CENTER);
        ventana.setSize(300, 150);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setVisible(true);

        try (Socket socket = new Socket(HOST, PUERTO);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            boton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!juegoTerminado) {
                        try {
                            out.writeUTF("AVANZAR");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            while (!juegoTerminado) {
                String mensaje = in.readUTF();
                System.out.println(mensaje);
                if (mensaje.equals("GANASTE")) {
                    estadoLabel.setText("¡Ganaste!");
                    juegoTerminado = true;
                    boton.setEnabled(false);
                } else if (mensaje.equals("PERDISTE")) {
                    estadoLabel.setText("Perdiste");
                    juegoTerminado = true;
                    boton.setEnabled(false);
                }
            }
        } catch (IOException e) {
            System.out.println("Este error");
            e.printStackTrace();
        }
    }
}
