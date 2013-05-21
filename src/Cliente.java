import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

public class Cliente
{
    private static int PORT = 0;
    private static String ADDRESS;
    private String sala = "antesala";

    private BufferedReader in;
    private PrintWriter out;
    private final JFrame frame = new JFrame("Aguacate");
    private final JTextField campoTexto = new JTextField(40);
    private final JTextArea mensajes = new JTextArea(8, 40);
    private final JScrollPane scroll = new JScrollPane(mensajes);

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

    private void getAddress() {
        Pattern patronAddress = Pattern.compile(
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        ADDRESS = JOptionPane.showInputDialog(frame, "Escriba la dirección del servidor", "Selección de servidor", JOptionPane.PLAIN_MESSAGE);
        Matcher matcher = patronAddress.matcher(ADDRESS);
        while (!matcher.find()) {
            getAddress();
        }
    }

    private void getPort() {
        PORT = Integer.parseInt(JOptionPane.showInputDialog(frame, "Escriba el puerto del servidor", "Selección de puerto", JOptionPane.PLAIN_MESSAGE));
        if (PORT > 65535 || PORT < 1) {
            getPort();
        }
    }

    private void run() throws IOException
    {
        getAddress();
        getPort();
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
