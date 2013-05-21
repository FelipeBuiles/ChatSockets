import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Cliente
{
    private static final int PORT = 6789;
    private String ADDRESS = "127.0.0.1";
    String sala = "antesala";

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Aguacate");
    JTextField campoTexto = new JTextField(40);
    JTextArea mensajes = new JTextArea(8, 40);
    JScrollPane scroll = new JScrollPane(mensajes);

    public Cliente()
    {
        campoTexto.setEditable(false);
        mensajes.setEditable(false);
        mensajes.setLineWrap(true);
        frame.add(campoTexto, "South");
        frame.add(scroll, "Center");
        frame.pack();

        campoTexto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String salida = campoTexto.getText();
                mensajes.setCaretPosition(mensajes.getDocument().getLength());
                if (salida.equals("-salir")) {
                    frame.dispose();
                } else if (salida.equals("-unirse")) {
                    salida = "ERROR404";
                }
                else if (salida.startsWith("-unirse ")) {
                    setSala(salida.substring(8));
                } else if (salida.equals("-salirsala")) {
                    sala = "antesala";
                }
                out.println(salida);
                campoTexto.setText("");
            }
        });
    }

    private void setSala(String sala) {
        this.sala = sala;
    }

    private String getNombre() {
        return JOptionPane.showInputDialog(frame," Escriba su nombre:", "Selección de nombre", JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException
    {
        Socket socket = new Socket(ADDRESS, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        String line;
        while(!(line = in.readLine()).equals(null)) {

            if (line.startsWith("SUBMIT")){
                out.println(getNombre());
            } else if (line.startsWith("ACCEPTED")){
                campoTexto.setEditable(true);
            } else if (line.startsWith("SERVER")) {
                mensajes.append(line.substring(7) + "\n");
            } else if ((line.startsWith("MESSAGE [")) && (line.substring(line.indexOf("[")+1, line.indexOf("]")).equals(sala))){
                mensajes.append(line.substring(7) + "\n");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Cliente cliente = new Cliente();
        cliente.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        cliente.frame.setVisible(true);
        cliente.run();
    }
}
