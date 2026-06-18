package uy.edu.um.doors;

import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.heap.EmptyHeapException;
import uy.edu.um.tad.queue.MyQueue;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.queue.EmptyQueueException;
import uy.edu.um.tad.stack.MyStack;
import uy.edu.um.tad.stack.MyStackImpl;
import uy.edu.um.tad.stack.EmptyStackException;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProcessManagerImpl implements ProcessManager {

    private MyQueue<Process> newProcessQueue;
    private MyHeap<Process> pendingProcessHeap;
    private Process runningProcess;
    private MyStack<Process> finishedStack;
    private int finishedCount;
    private MyHash<Integer, User> usersByUid;
    private MyHash<Integer, Process> processesByPid;

    private static final DateTimeFormatter LOG_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private String logFileName;

    public ProcessManagerImpl() {
        newProcessQueue = new MyQueueImpl<>();
        pendingProcessHeap = new MyHeapImpl<>(false);
        runningProcess = null;
        finishedStack = new MyStackImpl<>();
        finishedCount = 0;
        usersByUid = new MyHashImpl<>();
        processesByPid = new MyHashImpl<>();
        logFileName = "DOORS_PROCESS_LOG_" + LocalDateTime.now().format(LOG_DATE) + ".txt";
    }

    @Override
    public void loadProcessAndUserData(String processCsvPath, String usersCsvPath) {
        // Cargar usuarios desde el CSV
        try (java.io.BufferedReader lector = new java.io.BufferedReader(new java.io.FileReader(usersCsvPath))) {
            String linea = lector.readLine(); // saltar encabezado
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] partes = linea.split(";");
                int uid = Integer.parseInt(partes[0].trim());
                String alias = partes[1].trim();
                UserType tipo = UserType.valueOf(partes[2].trim());
                usersByUid.put(uid, new User(uid, alias, tipo));
            }
        } catch (java.io.IOException e) {
            System.out.println("Error al leer usuarios: " + e.getMessage());
        }

        // Cargar procesos desde el CSV
        try (java.io.BufferedReader lector = new java.io.BufferedReader(new java.io.FileReader(processCsvPath))) {
            String linea = lector.readLine(); // saltar encabezado
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                // Separar los campos por punto y coma
                int primerPuntoComa = linea.indexOf(';');
                int segundoPuntoComa = linea.indexOf(';', primerPuntoComa + 1);
                int tercerPuntoComa = linea.indexOf(';', segundoPuntoComa + 1);

                int pid = Integer.parseInt(linea.substring(0, primerPuntoComa).trim());
                int uid = Integer.parseInt(linea.substring(primerPuntoComa + 1, segundoPuntoComa).trim());
                String nombre = linea.substring(segundoPuntoComa + 1, tercerPuntoComa).trim();
                String eventosRaw = linea.substring(tercerPuntoComa + 1).trim();

                // Buscar el usuario dueño del proceso
                User usuario = usersByUid.get(uid);
                if (usuario == null) continue; // si no existe el usuario, ignorar el proceso

                Process proceso = new Process(pid, nombre, usuario);

                // Parsear los eventos: quitar llaves { }
                if (eventosRaw.startsWith("{")) eventosRaw = eventosRaw.substring(1);
                if (eventosRaw.endsWith("}")) eventosRaw = eventosRaw.substring(0, eventosRaw.length() - 1);

                // Separar cada evento por #
                String[] tokensEventos = eventosRaw.split("#");
                for (String token : tokensEventos) {
                    token = token.trim();
                    if (token.isEmpty()) continue;

                    // Separar tipo e instrucciones
                    int corcheteAbre = token.indexOf('[');
                    int corcheteCierra = token.indexOf(']');

                    String tipoStr = token.substring(0, corcheteAbre).replace(":", "").trim();
                    String instrStr = token.substring(corcheteAbre + 1, corcheteCierra).trim();

                    EventType tipoEvento = EventType.valueOf(tipoStr);
                    Event evento = new Event(tipoEvento);

                    // Agregar cada instrucción al evento
                    String[] instrucciones = instrStr.split(",");
                    for (String instr : instrucciones) {
                        evento.addInstruction(instr.trim());
                    }
                    proceso.addEvent(evento);
                }

                // Agregar el proceso a la cola de nuevos y al hash de búsqueda
                newProcessQueue.enqueue(proceso);
                processesByPid.put(pid, proceso);
            }
        } catch (java.io.IOException e) {
            System.out.println("Error al leer procesos: " + e.getMessage());
        }
    }

    private String ahora() {
        return LocalDateTime.now().format(LOG_TIMESTAMP);
    }

    private void escribirLog(String mensaje) {
        try (java.io.FileWriter fw = new java.io.FileWriter(logFileName, true)) {
            fw.write(mensaje + "\n");
        } catch (java.io.IOException e) {
            System.out.println("Error al escribir en el log: " + e.getMessage());
        }
    }



    @Override
    public void prepareProcesses() {
        try {
            // Mientras haya procesos nuevos en la cola
            while (!newProcessQueue.isEmpty()) {
                Process proceso = newProcessQueue.dequeue();

                // Calcular prioridad y cambiar estado a PENDING
                proceso.calculatePriority();
                proceso.setState(ProcessState.PENDING);

                // Insertar en el heap de pendientes
                pendingProcessHeap.insert(proceso);

                // Registrar en el log y mostrar por pantalla
                String mensaje = "[" + ahora() + "]: NEW PENDING PROCESS: PID=" + proceso.getPid()
                        + " | " + proceso.getName()
                        + " | USER:" + proceso.getUser().getAlias()
                        + " UID:" + proceso.getUser().getUid()
                        + " | P=" + proceso.getPriority();
                escribirLog(mensaje);
                System.out.println(mensaje);
            }
        } catch (EmptyQueueException e) {
            System.out.println("Error al procesar la cola: " + e.getMessage());
        }
    }

    @Override
    public void executeNextProcess() {
        // Verificar que no haya otro proceso en ejecución
        if (runningProcess != null) {
            System.out.println("Ya hay un proceso en ejecución: PID=" + runningProcess.getPid());
            return;
        }
        // Verificar que haya procesos pendientes
        if (pendingProcessHeap.isEmpty()) {
            System.out.println("No hay procesos pendientes para ejecutar.");
            return;
        }
        try {
            // Sacar el proceso de mayor prioridad del heap
            runningProcess = pendingProcessHeap.remove();
            runningProcess.setState(ProcessState.RUNNING);

            // Armar el mensaje con el proceso y sus eventos
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(ahora()).append("]: EXECUTING PROCESS: PID=").append(runningProcess.getPid())
                    .append(" | ").append(runningProcess.getUser());

            for (int i = 0; i < runningProcess.getEvents().size(); i++) {
                sb.append("\n").append(runningProcess.getEvents().get(i).toString());
            }

            escribirLog(sb.toString());
            System.out.println(sb.toString());

        } catch (EmptyHeapException e) {
            System.out.println("No hay procesos pendientes.");
        }
    }


    private void finalizarProceso(FinishType tipo, User terminadoPor) {
        // Verificar que haya un proceso en ejecución
        if (runningProcess == null) {
            System.out.println("No hay proceso en ejecución.");
            return;
        }

        // Actualizar estado del proceso
        runningProcess.setState(ProcessState.FINISHED);
        runningProcess.setFinishType(tipo);
        runningProcess.setTerminatedBy(terminadoPor);

        // Registrar en el log
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(ahora()).append("]: ENDING PROCESS: PID=").append(runningProcess.getPid())
                .append(" | STATE: ").append(tipo);
        if (tipo == FinishType.TERMINATED && terminadoPor != null) {
            sb.append(" by ").append(terminadoPor);
        }
        escribirLog(sb.toString());
        System.out.println(sb.toString());

        // Si la pila está llena, hacer overflow antes de agregar
        if (finishedCount >= MAX_FINISHED_PROCESS_ON_RAM) {
            // Registrar overflow en el log
            StringBuilder sbOverflow = new StringBuilder();
            sbOverflow.append("[").append(ahora()).append("]: Finished process stack overflow\n");

            // Vaciar la pila en una lista temporal para mostrar en orden inverso al de finalización
            java.util.ArrayList<Process> temp = new java.util.ArrayList<>();
            try {
                while (!finishedStack.isEmpty()) {
                    temp.add(finishedStack.pop());
                }
            } catch (EmptyStackException e) { }

            // temp tiene: [último finalizado, ..., primero finalizado]
            // orden inverso al de finalización = último primero, que ya es el orden de temp
            for (Process p : temp) {
                sbOverflow.append(p.toFinishedString()).append("\n");
            }

            escribirLog(sbOverflow.toString().trim());
            System.out.println(sbOverflow.toString().trim());
            finishedCount = 0;
        }

        // Agregar a la pila de finalizados
        finishedStack.push(runningProcess);
        finishedCount++;
        runningProcess = null;
    }



    @Override
    public void finishProcessOk() {
        finalizarProceso(FinishType.OK, null);
    }

    @Override
    public void finishProcessError() {
        finalizarProceso(FinishType.ERROR, null);
    }

    @Override
    public void terminateProcess(int uid) {
        User terminador = usersByUid.get(uid);
        if (terminador == null) {
            System.out.println("Usuario con UID=" + uid + " no encontrado.");
            return;
        }
        finalizarProceso(FinishType.TERMINATED, terminador);
    }

    @Override
    public void printStatus() {
        System.out.println("PROCESS STATUS");

        // Mostrar proceso en ejecución
        System.out.println("EXECUTING:");
        if (runningProcess != null) {
            System.out.println("\t" + runningProcess.toShortString());
        }

        // Mostrar procesos pendientes - hay que vaciar y restaurar el heap
        System.out.println("PENDING:");
        MyStack<Process> temp = new MyStackImpl<>();
        try {
            while (!pendingProcessHeap.isEmpty()) {
                Process p = pendingProcessHeap.remove();
                System.out.println("\t" + p.toShortString());
                temp.push(p);
            }
            // Restaurar el heap
            while (!temp.isEmpty()) {
                pendingProcessHeap.insert(temp.pop());
            }
        } catch (EmptyHeapException | EmptyStackException e) { }

        // Mostrar procesos finalizados - hay que vaciar y restaurar la pila
        System.out.println("FINISHED:");
        MyStack<Process> temp2 = new MyStackImpl<>();
        try {
            while (!finishedStack.isEmpty()) {
                Process p = finishedStack.pop();
                System.out.println("\t" + p.toFinishedString());
                temp2.push(p);
            }
            // Restaurar la pila
            while (!temp2.isEmpty()) {
                finishedStack.push(temp2.pop());
            }
        } catch (EmptyStackException e) { }
    }

    @Override
    public void printStatusVerbose() {
        System.out.println("PROCESS STATUS");

        // Mostrar proceso en ejecución con detalle de eventos
        System.out.println("EXECUTING:");
        if (runningProcess != null) {
            System.out.print("\t" + runningProcess.toVerboseString());
        }

        // Mostrar procesos pendientes con detalle de eventos
        System.out.println("PENDING:");
        MyStack<Process> temp = new MyStackImpl<>();
        try {
            while (!pendingProcessHeap.isEmpty()) {
                Process p = pendingProcessHeap.remove();
                System.out.print("\t" + p.toVerboseString());
                temp.push(p);
            }
            // Restaurar el heap
            while (!temp.isEmpty()) {
                pendingProcessHeap.insert(temp.pop());
            }
        } catch (EmptyHeapException | EmptyStackException e) { }

        // Mostrar procesos finalizados con detalle de eventos
        System.out.println("FINISHED:");
        MyStack<Process> temp2 = new MyStackImpl<>();
        try {
            while (!finishedStack.isEmpty()) {
                Process p = finishedStack.pop();
                System.out.print("\t" + p.toVerboseString());
                temp2.push(p);
            }
            // Restaurar la pila
            while (!temp2.isEmpty()) {
                finishedStack.push(temp2.pop());
            }
        } catch (EmptyStackException e) { }
    }

    @Override
    public void printStatusByUser(int uid) {
        User usuario = usersByUid.get(uid);
        if (usuario == null) {
            System.out.println("Usuario con UID=" + uid + " no encontrado.");
            return;
        }
        System.out.println("PROCESS STATUS - USER:" + usuario.getAlias() + " UID:" + uid);

        // Mostrar proceso en ejecución si pertenece al usuario
        System.out.println("EXECUTING:");
        if (runningProcess != null && runningProcess.getUser().getUid() == uid) {
            System.out.println("\t" + runningProcess.toShortString());
        }

        // Mostrar procesos pendientes del usuario - vaciar y restaurar el heap
        System.out.println("PENDING:");
        MyStack<Process> temp = new MyStackImpl<>();
        try {
            while (!pendingProcessHeap.isEmpty()) {
                Process p = pendingProcessHeap.remove();
                if (p.getUser().getUid() == uid) {
                    System.out.println("\t" + p.toShortString());
                }
                temp.push(p);
            }
            // Restaurar el heap
            while (!temp.isEmpty()) {
                pendingProcessHeap.insert(temp.pop());
            }
        } catch (EmptyHeapException | EmptyStackException e) { }

        // Mostrar procesos finalizados del usuario - vaciar y restaurar la pila
        System.out.println("FINISHED:");
        MyStack<Process> temp2 = new MyStackImpl<>();
        try {
            while (!finishedStack.isEmpty()) {
                Process p = finishedStack.pop();
                if (p.getUser().getUid() == uid) {
                    System.out.println("\t" + p.toFinishedString());
                }
                temp2.push(p);
            }
            // Restaurar la pila
            while (!temp2.isEmpty()) {
                finishedStack.push(temp2.pop());
            }
        } catch (EmptyStackException e) { }
    }

    @Override
    public void printStatusByProcess(int pid) {
        // Buscar el proceso por PID en el hash
        Process proceso = processesByPid.get(pid);
        if (proceso == null) {
            System.out.println("Proceso con PID=" + pid + " no encontrado en memoria.");
            return;
        }
        System.out.println(proceso.toVerboseString());
    }
}
