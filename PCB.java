import java.util.Objects;

class PCB implements Comparable<PCB> {
    // 私有属性
    private int ID;
    private int Runtime;
    private String Status;
    private int Priority;
    private String attribute;
    private int RequestSize;
    private int startAddress;

    // 基本的手动设置进程信息方法
    public PCB(int ProcessID, int Runtime, int Priority, String attribute, int RequestSize) {
        this.ID = ProcessID;
        this.Runtime = Runtime;
        this.Status = "reserve";
        this.Priority = Priority;
        this.attribute = attribute;
        this.RequestSize = RequestSize;
        this.startAddress = -1;
    }

    // Getter和Setter方法
    public int getID() {
        return ID;
    }

    public int getPriority() {
        return Priority;
    }

    public int getRuntime() {
        return Runtime;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getStatus() {
        return Status;
    }

    public int getRequestSize() {
        return RequestSize;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(int startAddress) {
        this.startAddress = startAddress;
    }

    public void setPriority(int priority) {
        Priority = priority;
    }

    public void setRuntime(int runtime) {
        Runtime = runtime;
    }

    public void setStatus(String status) {
        Status = status;
    }

    @Override
    public int compareTo(PCB other) {
        // 按照优先级从高到低排序（数值越大优先级越高）
        return Integer.compare(other.Priority, this.Priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PCB pcb = (PCB) o;
        return ID == pcb.ID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }
}
