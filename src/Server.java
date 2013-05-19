import java.io.*;
import java.net.*;
import java.util.HashSet;

public class Server {
    private static final int PORT = 6789;
    private static final int NUMUSUARIOS = 100;
    private static HashSet<String> usuarios = new HashSet<String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public static void main(String[] args) throws Exception {
        System.out.print(" ..Empezando...");
        ServerSocket socket = new ServerSocket(PORT, NUMUSUARIOS);
        try {
            while (true) {
                new Handler(socket.accept()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(" ..Cerrrando conexión.");
            socket.close();
        }
    }

    private static class Handler extends Thread {
        private String nombre;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while(true) {
                    out.println("SUBMIT");
                    nombre = in.readLine();
                    if (nombre == null) {
                        return;
                    }
                    synchronized (usuarios) {
                        if (!usuarios.contains(nombre)) {
                            usuarios.add(nombre);
                            break;
                        }
                    }
                }
                out.println("ACCEPTED");
                writers.add(out);
                String input;
                try {
                    while (!(input = in.readLine()).equals(null)) {
                        System.out.println(input);
                        if (input.equals("-quit")){
                            mostrarMensaje("SERVER-", nombre + " se ha desconectado.");
                            cerrarConexion();
                        } else {
                            mostrarMensaje(nombre, input);
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cerrarConexion();
            }
        }

        private void mostrarMensaje(String nombre, String input) {
            for (PrintWriter writer : writers) {
                writer.println("MESSAGE" + nombre + ": " + input);
            }
        }

        private void cerrarConexion() {
            if (nombre != null) {
                usuarios.remove(nombre);
            }
            if (out != null) {
                writers.remove(out);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}