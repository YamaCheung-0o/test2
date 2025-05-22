import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class OS implements MemoryManager.OSListener {
    private ConcurrentLinkedQueue<PCB> reserveQueue;
    private PriorityQueue<PCB> readyQueue; // 修改为 PriorityQueue
    private ConcurrentLinkedQueue<PCB> suspendQueue;

    private MemoryManager memoryManager;
    private int processorCount;
    private PCB[] runningProcesses;

    private final Object readyQueueLock = new Object();
    private final Object suspendQueueLock = new Object();
    private final Lock memoryLock = new ReentrantLock();


    public OS(int processorCount) {
        this.processorCount = processorCount;
        this.runningProcesses = new PCB[processorCount];

        // 初始化队列
        readyQueue = new PriorityQueue<>(Comparator.comparingInt(PCB::getPriority)); // 使用优先级比较器
        reserveQueue = new ConcurrentLinkedQueue<>();
        suspendQueue = new ConcurrentLinkedQueue<>();
        memoryManager = new MemoryManager();
        memoryManager.setOSListener(this);
    }

    // 手动添加进程
    public void addProcessManually(int processID, int runtime, int priority, String attribute, int requestSize) {
        PCB pcb = new PCB(processID, runtime, priority, attribute, requestSize);
        addProcess(pcb);
    }

    // 随机添加进程
    public void addProcessesRandomly(int num) {
        for (int i = 0; i < num; i++) {
            int processID = (int) (Math.random() * 100);
            int runtime = (int) (Math.random() * 10) + 1;
            int priority = (int) (Math.random() * 5) + 1;
            String[] attributes = {"CPU 密集型", "I/O 密集型"};
            String attribute = attributes[(int) (Math.random() * attributes.length)];
            int requestSize = (int) (Math.random() * 100) + 10;
            PCB pcb = new PCB(processID, runtime, priority, attribute, requestSize);
            addProcess(pcb);
        }
    }

    private void addProcess(PCB pcb) {
        // 设置进程状态
        pcb.setStatus("reserve");
        reserveQueue.add(pcb);
        System.out.println("进程 " + pcb.getID() + " 已加入后备队列，请求内存: " + pcb.getRequestSize() + "KB");

        // 尝试移动进程到就绪队列
        moveProcessesFromReserve();

        // 多处理器调度逻辑
        for (int i = 0; i < processorCount; i++) {
            PCB currentProcess = runningProcesses[i];
            PCB highestPriority = getHighestPriorityProcess();

            // 修改比较方向：数值越大优先级越高
            if (highestPriority != null &&
                    (currentProcess == null || highestPriority.getPriority() > currentProcess.getPriority())) {

                // 如果当前处理器有运行的进程，将其移入挂起队列
                if (currentProcess != null) {
                    currentProcess.setStatus("suspended");
                    synchronized (suspendQueueLock) {
                        suspendQueue.add(currentProcess);
                    }
                    System.out.println("进程 " + currentProcess.getID() + " 被抢占，移入挂起队列");
                }

                // 从就绪队列中移除最高优先级进程并分配给处理器
                runningProcesses[i] = readyQueue.poll();
                if (runningProcesses[i] != null) {
                    runningProcesses[i].setStatus("running");
                    System.out.println("进程 " + runningProcesses[i].getID() + " 被调度到处理器 " + i);
                }
            }
        }
    }

    // 获取最高优先级进程（数值越大优先级越高）
    private PCB getHighestPriorityProcess() {
        if (readyQueue.isEmpty()) {
            return null;
        }

        // 使用 max() 方法和反转的比较器
        return readyQueue.stream()
                .max(Comparator.comparingInt(PCB::getPriority))
                .orElse(null);
    }

    // 检查所有队列是否为空且没有运行中的进程
    public boolean areAllProcessesCompleted() {
        synchronized (readyQueueLock) {
            synchronized (suspendQueueLock) {
                return readyQueue.isEmpty() &&
                        reserveQueue.isEmpty() &&
                        suspendQueue.isEmpty() &&
                        Arrays.stream(runningProcesses).allMatch(Objects::isNull);
            }
        }
    }

    public void schedule(Runnable updateCallback) {
        // 为每个处理器创建独立线程
        Thread[] processorThreads = new Thread[processorCount];

        for (int i = 0; i < processorCount; i++) {
            final int processorId = i;
            processorThreads[i] = new Thread(() -> {
                while (true) {
                    try {
                        // 尝试获取就绪队列中的进程
                        PCB nextProcess = null;
                        synchronized (readyQueueLock) {
                            if (!readyQueue.isEmpty()) {
                                nextProcess = readyQueue.poll(); // 从队列头部获取
                            }
                        }

                        // 如果获取到进程，则执行
                        if (nextProcess != null) {
                            runningProcesses[processorId] = nextProcess;
                            nextProcess.setStatus("running");
                            System.out.println("处理器 " + processorId + " 开始执行进程 " + nextProcess.getID());

                            // 模拟进程执行一个时间片
                            Thread.sleep((Main.timeSlice) * 2000);

                            // 更新进程状态
                            synchronized (readyQueueLock) {
                                int remainingTime = nextProcess.getRuntime() - 1;
                                nextProcess.setRuntime(remainingTime);

                                if (remainingTime > 0) {
                                    // 进程未完成，降低优先级并放回就绪队列
                                    nextProcess.setPriority(nextProcess.getPriority() - 1);
                                    nextProcess.setStatus("ready");
                                    readyQueue.add(nextProcess); // PriorityQueue会自动排序
                                } else {
                                    // 进程完成，释放内存
                                    System.out.println("处理器 " + processorId + " 完成执行进程 " + nextProcess.getID());
                                    memoryManager.DeallocateMemory(nextProcess.getStartAddress());
                                }

                                runningProcesses[processorId] = null;
                            }

                            // 更新UI
                            if (updateCallback != null) {
                                updateCallback.run();
                            }
                        }
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
            });

            processorThreads[i].setName("Processor-" + i);
            processorThreads[i].start();
        }
    }

    // 在 OS.java 的 moveProcessesFromReserve 方法中添加日志
    public void moveProcessesFromReserve() {
        System.out.println("尝试从后备队列移动进程到就绪队列，当前后备队列大小: " + reserveQueue.size());

        // 输出当前内存状态
        System.out.println("当前内存状态:");
        for (MemoryManager.Partition p : memoryManager.getPartitionTable()) {
            System.out.println("地址: " + p.getStartAddress() + ", 大小: " + p.getLength() + ", 状态: " + p.getStatus());
        }

        // 使用迭代器安全遍历和修改队列
        Iterator<PCB> iterator = reserveQueue.iterator();
        while (iterator.hasNext()) {
            PCB pcb = iterator.next();
            if ("reserve".equals(pcb.getStatus())) {
                //内存锁确保在多线程环境下，同一时间只有一个线程可以尝试分配内存
                memoryLock.lock();
                try {
                    System.out.println("尝试为进程 " + pcb.getID() + " 分配内存，请求大小: " + pcb.getRequestSize() + "KB");
                    int startAddress = memoryManager.AllocateMemory(pcb.getRequestSize());
                    if (startAddress != -1) {
                        pcb.setStartAddress(startAddress);
                        pcb.setStatus("ready");
                        synchronized (readyQueueLock) {
                            readyQueue.add(pcb);
                        }
                        //使用迭代器的 remove() 方法从后备队列中移除该进程.
                        iterator.remove();
                        System.out.println("进程 " + pcb.getID() + " 已移至就绪队列，分配内存地址: " + startAddress);
                    }
                    else {
                        System.out.println("内存分配失败，进程 " + pcb.getID() + " 留在后备队列");
                    }
                }
                finally {
                    memoryLock.unlock();//解锁
                }
            }
        }
    }

    // 处理挂起队列
    @Override
    public void onMemoryFreed() {
        moveProcessesFromReserve();
        handleSuspendedQueue();
    }

    public void handleSuspendedQueue() {
        //创建一个临时列表，用于存储内存分配失败的进程，后续需要将这些进程重新放回挂起队列。
        List<PCB> failedToResume = new ArrayList<>();

        synchronized (suspendQueueLock) {
            while (!suspendQueue.isEmpty()) {
                PCB pcb = suspendQueue.poll();
                if ("suspended".equals(pcb.getStatus())) {
                    memoryLock.lock();
                    try {
                        int startAddress = memoryManager.AllocateMemory(pcb.getRequestSize());
                        if (startAddress != -1) {
                            pcb.setStartAddress(startAddress);
                            pcb.setStatus("ready");
                            readyQueue.add(pcb);
                            System.out.println("进程 " + pcb.getID() + " 恢复成功");
                        }
                        else {
                            // 内存不足，无法恢复
                            pcb.setStatus("suspended"); // 确保状态正确
                            failedToResume.add(pcb);
                            System.out.println("进程 " + pcb.getID() + " 恢复失败：内存不足");
                        }
                    } finally {
                        memoryLock.unlock();
                    }
                } else {
                    // 非挂起状态的进程不应该出现在挂起队列中
                    System.err.println("警告：发现非挂起状态的进程在挂起队列中 ID=" + pcb.getID() + " 状态=" + pcb.getStatus());
                    if (pcb.getStatus().equals("ready")) {
                        synchronized (readyQueueLock) {
                            readyQueue.add(pcb);
                        }
                    }
                    else if (pcb.getStatus().equals("running")) {
                        // 找到空闲的处理器并将进程放入
                        for (int i = 0; i < runningProcesses.length; i++) {
                            if (runningProcesses[i] == null) {
                                runningProcesses[i] = pcb;
                                break;
                            }
                        }
                    }
                }
            }

            // 将无法恢复的进程重新加入挂起队列
            suspendQueue.addAll(failedToResume);
        }
    }

    // 处理所有进程完成的情况
    public void handleAllProcessesCompleted(Runnable updateCallback) {
        // 重置内存
        memoryManager.resetMemory();

        // 清空所有队列
        readyQueue.clear();
        reserveQueue.clear();
        suspendQueue.clear();
        Arrays.fill(runningProcesses, null);

        System.out.println("所有进程已完成，内存已重置");

        // 更新 UI
        if (updateCallback != null) {
            updateCallback.run();
        }
    }

    // Getter方法
    public Queue<PCB> getReserveQueue() {
        return reserveQueue;
    }

    public PriorityQueue<PCB> getReadyQueue() {
        return readyQueue;
    }

    public Queue<PCB> getSuspendQueue() {
        return suspendQueue;
    }

    public PCB[] getRunningProcesses() {
        return runningProcesses;
    }

    // 添加获取分区表的方法
    public List<MemoryManager.Partition> getPartitionTable() {
        // 返回分区表的副本，避免外部直接修改
        return new ArrayList<>(MemoryManager.PartitionTable);
    }

    // 添加获取MemoryManager的方法
    public MemoryManager getMemoryManager() {
        return memoryManager;
    }
}