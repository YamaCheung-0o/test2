import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MemoryManager {
    // 基本私人属性,单位是kb
    private static final int MainMemorySize = 1024;
    private static final int OSSize = 128;

    // 用构造函数初始化分区表
    public MemoryManager() {
        PartitionTable = Collections.synchronizedList(new ArrayList<>());
        // 初始化分区表，将操作系统占用的部分设置好，剩余部分作为一个未分配分区
        PartitionTable.add(new Partition(0, OSSize, "已分"));
        PartitionTable.add(new Partition(OSSize, MainMemorySize - OSSize, "未分"));
    }
    // 添加重置内存的方法
    public void resetMemory() {
        synchronized (PartitionTable) {
            PartitionTable.clear();
            PartitionTable.add(new Partition(0, OSSize, "已分"));
            PartitionTable.add(new Partition(OSSize, MainMemorySize - OSSize, "未分"));
        }
    }

    // 创建一个list来存储分区表
    public static List<Partition> PartitionTable;

    // 每个分区类的基本属性和方法
    static class Partition {
        private int StartAddress;
        private int Length;
        private String Status;

        // 初始化分区分配
        public Partition(int startAddress, int length, String status) {
            this.StartAddress = startAddress;
            this.Length = length;
            this.Status = status;
        }

        // 获取分区具体信息
        public int getStartAddress() {
            return StartAddress;
        }

        public int getLength() {
            return Length;
        }

        public String getStatus() {
            return Status;
        }

        public void setStatus(String status) {
            this.Status = status;
        }

        public void setLength(int length) {
            Length = length;
        }

        public void setStartAddress(int startAddress) {
            StartAddress = startAddress;
        }
    }


    // 修改 MemoryManager.java 中的 AllocateMemory 方法
    public int AllocateMemory(int RequestSize) {
        for (int i = 0; i < PartitionTable.size(); i++) {
            Partition partition = PartitionTable.get(i);
            if ("未分".equals(partition.getStatus())) {
                if (partition.getLength() >= RequestSize) {
                    int startAddress = partition.getStartAddress();
                    if (partition.getLength() > RequestSize) {
                        // 创建新的空闲分区
                        int newFreeStart = startAddress + RequestSize;
                        int newFreeLength = partition.getLength() - RequestSize;
                        PartitionTable.add(i + 1, new Partition(newFreeStart, newFreeLength, "未分"));

                        // 更新当前分区为已分配
                        partition.setStatus("已分");
                        partition.setLength(RequestSize);
                    } else {
                        // 如果分区大小刚好等于请求大小，直接标记为已分配
                        partition.setStatus("已分");
                    }
                    return startAddress;
                }
            }
        }
        return -1;
    }



    // 内存回收
    public void DeallocateMemory(int startAddress) {
        for (int i = 0; i < PartitionTable.size(); i++) {
            Partition partition = PartitionTable.get(i);
            if (partition.getStatus().equals("已分") && partition.getStartAddress() == startAddress) {
                partition.setStatus("未分");
                MergePartitions(i);


                //尝试将后备队列中的进程移入就绪队列
                // 内存释放后通知OS尝试解挂
                if (osListener != null) {
                    osListener.onMemoryFreed();
                }
                break;
            }
        }
    }

    // 内存合并
    private void MergePartitions(int index) {
        boolean merged = true;
        while (merged) {
            merged = false;
            // 检查前一个分区
            if (index > 0) {
                Partition prev = PartitionTable.get(index - 1);
                Partition current = PartitionTable.get(index);
                if ("未分".equals(prev.getStatus()) && "未分".equals(current.getStatus())) {
                    int newStart = prev.getStartAddress();
                    int newLength = prev.getLength() + current.getLength();
                    prev.setStartAddress(newStart);
                    prev.setLength(newLength);
                    PartitionTable.remove(index);
                    index--;
                    merged = true;
                }
            }
            // 检查后一个分区
            if (index < PartitionTable.size() - 1) {
                Partition current = PartitionTable.get(index);
                Partition next = PartitionTable.get(index + 1);
                if ("未分".equals(current.getStatus()) && "未分".equals(next.getStatus())) {
                    int newStart = current.getStartAddress();
                    int newLength = current.getLength() + next.getLength();
                    current.setStartAddress(newStart);
                    current.setLength(newLength);
                    PartitionTable.remove(index + 1);
                    merged = true;
                }
            }
        }
    }

    // 添加OS监听器接口和设置方法
    private OSListener osListener;

    public void setOSListener(OSListener listener) {
        this.osListener = listener;
    }

    public interface OSListener {
        void onMemoryFreed();
    }

    // 在 MemoryManager.java 中添加
    public List<Partition> getPartitionTable() {
        return new ArrayList<>(PartitionTable);
    }
}
