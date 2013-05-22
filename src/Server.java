import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final HashMap<String, String> usuarios = new HashMap<String, String>();
    private static final HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    private static final HashSet<String> salas = new HashSet<String>();
    private static final ArrayList<String> salasABorrar = new ArrayList<String>();

    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(System.in);
        System.out.println(" ..Puerto?");
        int PORT = s.nextInt();
        System.out.println(" ..Numero de usuarios?");
        int NUMUSUARIOS = s.nextInt();
        System.out.println(" ..Empezando...");
        ServerSocket socket = new ServerSocket(PORT, NUMUSUARIOS);
        try {
            //noinspection InfiniteLoopStatement
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

    @SuppressWarnings("ConstantConditions")
    private static class Handler extends Thread {
        private String nombre;
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String sala;

        public Handler(Socket socket) {
            this.socket = socket;
            sala = "antesala";
        }

        @SuppressWarnings("ConstantConditions")
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String antesala = "antesala";
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
                        } else {
                            out.println("ERROR101");
                        }
                    }
                }
                out.println("ACCEPTED");
                writers.add(out);
                String input;
                try {
                    while (!((input = in.readLine()) == null)) {
                        if (input.equals("-salir")) {
                            broadcast("SERVER-", nombre + " se ha desconectado.");
                            cerrarConexion();
                        } else if (input.equals("-listarusuarios")) {
                                out.println("SERVER- Usuarios:");
                            for (Map.Entry<String, String> usuario : usuarios.entrySet()) {
                                out.println("SERVER-" + usuario.getKey() + " - " + usuario.getValue());
                            }
                        } else if (input.equals("-listarsalas")) {
                            if (salas.isEmpty()){
                                out.println("SERVER- No hay salas.");
                            } else {
                                out.println("SERVER- Salas:");
                            }
                            for (String sala : salas) {
                                int cont = 0;
                                for(Map.Entry<String, String> usuario : usuarios.entrySet()) {
                                    if (usuario.getValue().equals(sala)) {
                                        cont++;
                                    }
                                }
                                if (cont == 0) {
                                    salasABorrar.add(sala);
                                }
                                out.println("SERVER-"+ sala + " : " + cont);
                            }
                            for (String sala : salasABorrar) {
                                salas.remove(sala);
                            }
                        } else if (input.startsWith("-unirse")) {
                            sala = input.substring(8);
                            if (!salas.contains(sala)) {
                                salas.add(sala);
                            } if (usuarios.get(nombre).equals(sala)) {
                                out.println("SERVER- Ya perteneces a esta sala.");
                            } else {
                                usuarios.put(nombre, sala);
                                out.println("SERVER- Te has unido a " + sala + ".");
                            }
                        } else if (input.equals("-salirsala")) {
                            out.println("SERVER- Has salido exitosamente de la sala.");
                            usuarios.put(nombre, antesala);
                        } else if (input.equals("ERROR404")) {
                            out.println("SERVER- Por favor ingrese el nombre de la sala.");
                        }

                        else {
                            broadcast(nombre, input);
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

        private void broadcast(String nombre, String input) {
            for (PrintWriter writer : writers) {
                writer.println("MESSAGE " + "[" + usuarios.get(nombre) + "] " + nombre + ": " + input);
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