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
                    out.println(" ..Nombre de Usuario?");
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
                out.println(" ..Registro exitoso.");
                writers.add(out);
                while (true) {
                    String input = in.readLine();
                    if(input == null) {
                        return;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println(nombre + ": " + input);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                for(PrintWriter writer : writers) {
                    writer.println(" .." + nombre + "se ha desconectado.");
                }
                if (nombre != null) {
                    usuarios.remove(nombre);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}