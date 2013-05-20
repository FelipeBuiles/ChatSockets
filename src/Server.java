import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Server {
    private static final int PORT = 6789;
    private static final int NUMUSUARIOS = 100;
    private static HashMap<String, String> usuarios = new HashMap<String, String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    private static HashSet<String> salas = new HashSet<String>();
    private static String antesala = "antesala";

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
        private String sala;

        public Handler(Socket socket) {
            this.socket = socket;
            sala = "antesala";
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
                        if (!usuarios.containsKey(nombre)) {
                            usuarios.put(nombre, antesala);
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
                        if (input.equals("-salir")) {
                            mostrarMensaje("SERVER-", nombre + " se ha desconectado.");
                            cerrarConexion();
                        } else if (input.equals("-listarusuarios")) {
                            for (Map.Entry<String, String> usuario : usuarios.entrySet()) {
                                out.println("MESSAGE" + usuario.getKey() + " - " + usuario.getValue());
                            }
                        } else if (input.equals("-listarsalas")) {
                            for (String sala : salas) {
                                out.println("MESSAGE" + sala);
                            }
                        } else if (input.startsWith("-unirse")) {
                            sala = input;
                            System.out.print(sala);
                            if (sala == null) {
                                return;
                            }
                            synchronized (salas) {
                                if (!salas.contains(sala)) {
                                    salas.add(sala);
                                    break;
                                }
                            }
                            usuarios.put(nombre, sala);
                        }

                        else {
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