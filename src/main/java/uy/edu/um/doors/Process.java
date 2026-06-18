package uy.edu.um.doors;

import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;

public class Process implements Comparable<Process> {
    private int pid;
    private String name;
    private User user;
    private int priority;
    private ProcessState state;
    private MyList<Event> events;
    private FinishType finishType;
    private User terminatedBy;

    public Process(int pid, String name, User user) {
        this.pid = pid;
        this.name = name;
        this.user = user;
        this.state = ProcessState.NEW;
        this.events = new MyLinkedListImpl<>();
        this.priority = 0;
        this.finishType = null;
        this.terminatedBy = null;
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public void calculatePriority() {
        int nCpu = 0, nRam = 0, nDisk = 0;
        for (int i = 0; i < events.size(); i++) {
            EventType t = events.get(i).getType();
            if (t == EventType.CPU) nCpu++;
            else if (t == EventType.RAM) nRam++;
            else if (t == EventType.DISK) nDisk++;
        }
        int nEvents = events.size();
        if (nEvents == 0) {
            this.priority = 0;
            return;
        }
        this.priority = (8 * nCpu + 2 * nRam + 2 * nDisk) / nEvents + user.getWeight() * nEvents;
    }

    @Override
    public int compareTo(Process other) {
        return Integer.compare(this.priority, other.priority);
    }

    public int getPid() { return pid; }
    public String getName() { return name; }
    public User getUser() { return user; }
    public int getPriority() { return priority; }
    public ProcessState getState() { return state; }
    public MyList<Event> getEvents() { return events; }
    public FinishType getFinishType() { return finishType; }
    public User getTerminatedBy() { return terminatedBy; }

    public void setState(ProcessState state) { this.state = state; }
    public void setFinishType(FinishType finishType) { this.finishType = finishType; }
    public void setTerminatedBy(User terminatedBy) { this.terminatedBy = terminatedBy; }

    public String toShortString() {
        return "PID=" + pid + " | " + name + " | " + user + " | P=" + priority;
    }

    public String toFinishedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PID=").append(pid).append(" ").append(name)
                .append(" | STATE: ").append(finishType);
        if (finishType == FinishType.TERMINATED && terminatedBy != null) {
            sb.append(" | ").append(terminatedBy);
        } else {
            sb.append(" | ").append(user);
        }
        return sb.toString();
    }

    public String toVerboseString() {
        StringBuilder sb = new StringBuilder();
        sb.append(toShortString()).append("\n");
        for (int i = 0; i < events.size(); i++) {
            sb.append(events.get(i).toString()).append("\n");
        }
        return sb.toString();
    }
}