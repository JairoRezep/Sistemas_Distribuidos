import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

public class Servidor {
    private static final int PUERTO = 12345;
    private static Map<String, JProgressBar> progresoClientes = new HashMap<>();
    private static JFrame ventana;
    private static JPanel panel;
    private static boolean juegoTerminado = false;

    public static void main(String[] args) {
        ventana = new JFrame("Servidor - Progreso de Clientes");
        panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        ventana.add(panel);
        ventana.setSize(400, 300);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setVisible(true);

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            while (!juegoTerminado) {
                Socket clienteSocket = serverSocket.accept();
                String nombreCliente = "Cliente" + (progresoClientes.size() + 1);
                JProgressBar barra = new JProgressBar(0, 100);
                barra.setStringPainted(true);
                progresoClientes.put(nombreCliente, barra);

                panel.add(new JLabel(nombreCliente));
                panel.add(barra);
                panel.revalidate();

                new Thread(new ManejadorCliente(clienteSocket, nombreCliente)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ManejadorCliente implements Runnable {
        private Socket clienteSocket;
        private String nombreCliente;

        public ManejadorCliente(Socket clienteSocket, String nombreCliente) {
            this.clienteSocket = clienteSocket;
            this.nombreCliente = nombreCliente;
        }

        @Override
        public void run() {
            try (DataInputStream in = new DataInputStream(clienteSocket.getInputStream());
                 DataOutputStream out = new DataOutputStream(clienteSocket.getOutputStream())) {

                while (!juegoTerminado) {
                    String mensaje = in.readUTF();
                    if (mensaje.equals("AVANZAR")) {
                        int progreso = (int) (Math.random() * 6) + 10;
                        JProgressBar barra = progresoClientes.get(nombreCliente);
                        int nuevoValor = barra.getValue() + progreso;

                        if (nuevoValor >= 100) {
                            barra.setValue(100);
                            notificarResultado(nombreCliente);
                            juegoTerminado = true;
                            break;
                        } else {
                            barra.setValue(nuevoValor);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void notificarResultado(String ganador) {
            for (Map.Entry<String, JProgressBar> entry : progresoClientes.entrySet()) {
                String cliente = entry.getKey();
                try (Socket socket = new Socket(clienteSocket.getInetAddress(), 12346);
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                    if (cliente.equals(ganador)) {
                        out.writeUTF("GANASTE");
                    } else {
                        out.writeUTF("PERDISTE");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
