import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Cliente
{
    private static final int PORT = 6789;
    private String ADDRESS = "127.0.0.1";

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Aguacate");
    JTextField campoTexto = new JTextField(40);
    JTextArea mensajes = new JTextArea(8, 40);

    public Cliente()
    {
        campoTexto.setEditable(false);
        mensajes.setEditable(false);
        frame.add(campoTexto, "North");
        frame.add(new JScrollPane(campoTexto), "Center");
        frame.pack();

        campoTexto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(campoTexto.getText());
                campoTexto.setText("");
            }
        });
    }

    private String getNombre() {
        return JOptionPane.showInputDialog(frame," Escriba su nombre:", "Seleccion de nombre", JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException
    {
        Socket socket = new Socket(ADDRESS, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        while(true)
        {
            String line = in.readLine();
            System.out.println(line);
            if (line.startsWith("SUBMIT")){
                out.println(getNombre());
            } else if (line.startsWith("ACCEPTED")){
                campoTexto.setEditable(true);
            } else if (line.startsWith("MESSAGE")){
                mensajes.append(line.substring(8) + "\n");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Cliente cliente = new Cliente();
        cliente.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cliente.frame.setVisible(true);
        cliente.run();
    }
}
