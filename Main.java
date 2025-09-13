import javax.swing.*;
import java.awt.*;
public class Main {
    // 设置时间片为1秒 (修正为毫秒单位)
    public static int timeSlice = 1;

    public static void main(String args[]) {
        // 设置处理器数量为2核
        OS curOS = new OS(2);

        // 创建主窗口
        JFrame frame = new JFrame("多处理器进程调度系统");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        // 创建面板并添加到窗口
        Panel panel = new Panel(curOS, frame);
        frame.add(panel);

        // 显示窗口
        frame.setVisible(true);

        // 启动调度器（只启动一次）
        curOS.schedule(() -> {
            //确保在事件调度线程中更新 GUI 组件，包括正在运行的进程表、就绪队列表、后备队列表、挂起队列表和内存表。
            SwingUtilities.invokeLater(() -> {
                panel.updateRunningProcessTable();
                panel.updateQueueTable("ready", panel.getReadyQueueTableModel());
                panel.updateQueueTable("reserve", panel.getReserveQueueTableModel());
                panel.updateQueueTable("suspend", panel.getSuspendQueueTableModel());
                panel.updateMemoryTable(); // 添加内存表更新
            });
        });

        // 创建一个单独的线程来定期检查队列状态
        Thread checkQueueThread = new Thread(() -> {
            while (true) {
                try {
                    // 每隔一段时间检查一次
                    Thread.sleep(5000);

                    if (curOS.areAllProcessesCompleted()) {
                        curOS.handleAllProcessesCompleted(() -> {
                            SwingUtilities.invokeLater(() -> {
                                panel.updateRunningProcessTable();
                                panel.updateQueueTable("ready", panel.getReadyQueueTableModel());
                                panel.updateQueueTable("reserve", panel.getReserveQueueTableModel());
                                panel.updateQueueTable("suspend", panel.getSuspendQueueTableModel());
                                panel.updateMemoryTable(); // 添加内存表更新
                            });
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });
        checkQueueThread.start();
    }
}
