import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;

class Panel extends JPanel {
    private JButton manualAddButton;
    private JButton randomAddButton;
    private JButton viewReadyQueueButton;
    private JButton viewReserveQueueButton;
    private JButton viewSuspendQueueButton;
    private JButton viewMemoryButton;
    private JTabbedPane tabbedPane;
    private JPanel runningProcessPanel;
    private DefaultTableModel runningProcessTableModel;
    private JTable runningProcessTable;
    private JPanel readyQueuePanel;
    private DefaultTableModel readyQueueTableModel;
    private JTable readyQueueTable;
    private JPanel reserveQueuePanel;
    private DefaultTableModel reserveQueueTableModel;
    private JTable reserveQueueTable;
    private JPanel suspendQueuePanel;
    private DefaultTableModel suspendQueueTableModel;
    private JTable suspendQueueTable;
    private JPanel memoryPanel;
    private DefaultTableModel memoryTableModel;
    private JTable memoryTable;
    private OS CurOS;
    private JFrame frame;

    // 内存状态标签
    private JLabel memoryStatusLabel;

    public Panel(OS CurOS, JFrame frame) {
        this.CurOS = CurOS;
        this.frame = frame;
        setLayout(new BorderLayout());

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        manualAddButton = new JButton("手动添加进程");
        randomAddButton = new JButton("随机添加进程");
        viewReadyQueueButton = new JButton("查看就绪队列");
        viewReserveQueueButton = new JButton("查看后备队列");
        viewSuspendQueueButton = new JButton("查看挂起队列");
        viewMemoryButton = new JButton("查看内存状态");

        buttonPanel.add(manualAddButton);
        buttonPanel.add(randomAddButton);
        buttonPanel.add(viewReadyQueueButton);
        buttonPanel.add(viewReserveQueueButton);
        buttonPanel.add(viewSuspendQueueButton);
        buttonPanel.add(viewMemoryButton);
        add(buttonPanel, BorderLayout.NORTH);

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();

        // 运行中进程面板
        runningProcessPanel = new JPanel(new BorderLayout());
        JLabel runningProcessLabel = new JLabel("运行中进程");
        runningProcessLabel.setFont(new Font("宋体", Font.BOLD, 14));
        runningProcessPanel.add(runningProcessLabel, BorderLayout.NORTH);

        runningProcessTableModel = new DefaultTableModel(
                new String[]{"处理器", "进程ID", "运行时间", "优先级", "属性", "内存地址"}, 0);
        runningProcessTable = new JTable(runningProcessTableModel);

        runningProcessTable.setGridColor(Color.LIGHT_GRAY);
        runningProcessTable.setShowGrid(true);
        runningProcessTable.setRowHeight(25);

        runningProcessTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(Color.YELLOW);
                c.setForeground(Color.BLACK);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }
        });

        JScrollPane runningProcessScrollPane = new JScrollPane(runningProcessTable);
        runningProcessPanel.add(runningProcessScrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("运行中进程", runningProcessPanel);

        // 就绪队列面板
        readyQueuePanel = new JPanel(new BorderLayout());
        JLabel readyQueueLabel = new JLabel("就绪队列进程信息");
        readyQueueLabel.setFont(new Font("宋体", Font.BOLD, 14));
        readyQueuePanel.add(readyQueueLabel, BorderLayout.NORTH);

        readyQueueTableModel = new DefaultTableModel(
                new String[]{"ID", "运行时间", "优先级", "属性", "请求内存大小", "状态", "内存地址"}, 0);
        readyQueueTable = new JTable(readyQueueTableModel);

        readyQueueTable.setGridColor(Color.LIGHT_GRAY);
        readyQueueTable.setShowGrid(true);
        readyQueueTable.setRowHeight(25);

        JScrollPane readyQueueScrollPane = new JScrollPane(readyQueueTable);
        readyQueuePanel.add(readyQueueScrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("就绪队列", readyQueuePanel);

        // 后备队列面板
        reserveQueuePanel = new JPanel(new BorderLayout());
        JLabel reserveQueueLabel = new JLabel("后备队列进程信息");
        reserveQueueLabel.setFont(new Font("宋体", Font.BOLD, 14));
        reserveQueuePanel.add(reserveQueueLabel, BorderLayout.NORTH);

        reserveQueueTableModel = new DefaultTableModel(
                new String[]{"ID", "运行时间", "优先级", "属性", "请求内存大小", "状态", "内存地址"}, 0);
        reserveQueueTable = new JTable(reserveQueueTableModel);

        reserveQueueTable.setGridColor(Color.LIGHT_GRAY);
        reserveQueueTable.setShowGrid(true);
        reserveQueueTable.setRowHeight(25);

        JScrollPane reserveQueueScrollPane = new JScrollPane(reserveQueueTable);
        reserveQueuePanel.add(reserveQueueScrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("后备队列", reserveQueuePanel);

        // 挂起队列面板
        suspendQueuePanel = new JPanel(new BorderLayout());
        JLabel suspendQueueLabel = new JLabel("挂起队列进程信息");
        suspendQueueLabel.setFont(new Font("宋体", Font.BOLD, 14));
        suspendQueuePanel.add(suspendQueueLabel, BorderLayout.NORTH);

        suspendQueueTableModel = new DefaultTableModel(
                new String[]{"ID", "运行时间", "优先级", "属性", "请求内存大小", "状态", "内存地址"}, 0);
        suspendQueueTable = new JTable(suspendQueueTableModel);

        suspendQueueTable.setGridColor(Color.LIGHT_GRAY);
        suspendQueueTable.setShowGrid(true);
        suspendQueueTable.setRowHeight(25);

        JScrollPane suspendQueueScrollPane = new JScrollPane(suspendQueueTable);
        suspendQueuePanel.add(suspendQueueScrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("挂起队列", suspendQueuePanel);

        // 内存状态面板
        memoryPanel = new JPanel(new BorderLayout());
        JLabel memoryLabel = new JLabel("内存状态");
        memoryLabel.setFont(new Font("宋体", Font.BOLD, 14));
        memoryPanel.add(memoryLabel, BorderLayout.NORTH);

        memoryTableModel = new DefaultTableModel(
                new String[]{"起始地址", "大小(KB)", "状态"}, 0);
        memoryTable = new JTable(memoryTableModel);

        memoryTable.setGridColor(Color.LIGHT_GRAY);
        memoryTable.setShowGrid(true);
        memoryTable.setRowHeight(25);

        // 设置不同状态的颜色
        memoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // 获取状态列的值
                String status = (String) table.getValueAt(row, 2);

                // 根据状态设置不同的背景色
                if ("已分".equals(status)) {
                    c.setBackground(Color.RED);
                    c.setForeground(Color.WHITE);
                } else if ("未分".equals(status)) {
                    c.setBackground(Color.GREEN);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }

                return c;
            }
        });

        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);
        memoryPanel.add(memoryScrollPane, BorderLayout.CENTER);

        // 初始化内存状态标签
        memoryStatusLabel = new JLabel("内存状态: 初始化完成");
        memoryStatusLabel.setForeground(Color.BLUE);
        memoryStatusLabel.setHorizontalAlignment(JLabel.CENTER);
        memoryPanel.add(memoryStatusLabel, BorderLayout.SOUTH);

        tabbedPane.addTab("内存状态", memoryPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // 按钮事件处理
        manualAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(frame, "请依次输入进程的 ID、运行时间、优先级、属性、请求内存大小，用空格分隔：");
                if (input != null) {
                    String[] inputs = input.split(" ");
                    if (inputs.length == 5) {
                        try {
                            int processID = Integer.parseInt(inputs[0]);
                            int runtime = Integer.parseInt(inputs[1]);
                            int priority = Integer.parseInt(inputs[2]);
                            String attribute = inputs[3];
                            int requestSize = Integer.parseInt(inputs[4]);

                            CurOS.addProcessManually(processID, runtime, priority, attribute, requestSize);

                            // 更新所有表格
                            updateAllTables();

                            tabbedPane.setSelectedIndex(0); // 切换到运行中进程选项卡
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame, "输入的数字格式不正确，请重新输入！", "输入错误", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "输入的参数数量不正确，请重新输入！", "输入错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        randomAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String numStr = JOptionPane.showInputDialog(frame, "请输入要随机添加的进程数量：");
                if (numStr != null && !numStr.isEmpty()) {
                    try {
                        int num = Integer.parseInt(numStr);
                        CurOS.addProcessesRandomly(num);

                        // 更新所有表格
                        updateAllTables();

                        tabbedPane.setSelectedIndex(0); // 切换到运行中进程选项卡
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "输入的数字格式不正确，请重新输入！", "输入错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // 查看就绪队列按钮事件
        viewReadyQueueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateQueueTable("ready", readyQueueTableModel);
                tabbedPane.setSelectedIndex(1); // 切换到就绪队列选项卡
            }
        });

        // 查看后备队列按钮事件
        viewReserveQueueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateQueueTable("reserve", reserveQueueTableModel);
                tabbedPane.setSelectedIndex(2); // 切换到后备队列选项卡
            }
        });

        // 查看挂起队列按钮事件
        viewSuspendQueueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateQueueTable("suspend", suspendQueueTableModel);
                tabbedPane.setSelectedIndex(3); // 切换到挂起队列选项卡
            }
        });

        // 内存状态按钮事件
        viewMemoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMemoryTable(); // 更新内存表
                tabbedPane.setSelectedIndex(4); // 切换到内存状态选项卡
            }
        });

        // 初始化显示
        updateRunningProcessTable();
        updateQueueTable("ready", readyQueueTableModel);
        updateQueueTable("reserve", reserveQueueTableModel);
        updateQueueTable("suspend", suspendQueueTableModel);
        updateMemoryTable(); // 初始化内存表
    }

    void updateRunningProcessTable() {
        runningProcessTableModel.setRowCount(0);
        PCB[] runningProcesses = CurOS.getRunningProcesses();

        for (int i = 0; i < runningProcesses.length; i++) {
            PCB process = runningProcesses[i];
            if (process != null) {
                runningProcessTableModel.addRow(new Object[]{
                        i,
                        process.getID(),
                        process.getRuntime(),
                        process.getPriority(),
                        process.getAttribute(),
                        process.getStartAddress()
                });
            }
        }

        // 如果没有运行中的进程，显示空状态
        if (runningProcessTableModel.getRowCount() == 0) {
            runningProcessTableModel.addRow(new Object[]{"所有处理器空闲", "", "", "", "", ""});
        }
    }

    void updateQueueTable(String queueType, DefaultTableModel tableModel) {
        tableModel.setRowCount(0);

        // 获取原始队列
        switch (queueType) {
            case "ready":
                // 处理就绪队列 (PriorityQueue)
                ArrayList<PCB> readyList;
                synchronized (CurOS.getReadyQueue()) {
                    readyList = new ArrayList<>(CurOS.getReadyQueue());
                }
                // PriorityQueue已经按优先级排序，无需再次排序
                for (PCB pcb : readyList) {
                    tableModel.addRow(new Object[]{
                            pcb.getID(), pcb.getRuntime(), pcb.getPriority(),
                            pcb.getAttribute(), pcb.getRequestSize(),
                            pcb.getStatus(), pcb.getStartAddress()
                    });
                }
                break;

            case "reserve":
                // 处理后备队列
                ArrayList<PCB> reserveList;
                synchronized (CurOS.getReserveQueue()) {
                    reserveList = new ArrayList<>(CurOS.getReserveQueue());
                }
                for (PCB pcb : reserveList) {
                    tableModel.addRow(new Object[]{
                            pcb.getID(), pcb.getRuntime(), pcb.getPriority(),
                            pcb.getAttribute(), pcb.getRequestSize(),
                            pcb.getStatus(), pcb.getStartAddress()
                    });
                }
                break;

            case "suspend":
                // 处理挂起队列
                ArrayList<PCB> suspendList;
                synchronized (CurOS.getSuspendQueue()) {
                    suspendList = new ArrayList<>(CurOS.getSuspendQueue());
                }
                for (PCB pcb : suspendList) {
                    tableModel.addRow(new Object[]{
                            pcb.getID(), pcb.getRuntime(), pcb.getPriority(),
                            pcb.getAttribute(), pcb.getRequestSize(),
                            pcb.getStatus(), pcb.getStartAddress()
                    });
                }
                break;
        }
    }

    // 更新内存表
    void updateMemoryTable() {
        memoryTableModel.setRowCount(0);

        // 获取内存分区表
        ArrayList<MemoryManager.Partition> partitions = new ArrayList<>(CurOS.getMemoryManager().getPartitionTable());

        // 按起始地址排序
        partitions.sort(Comparator.comparingInt(MemoryManager.Partition::getStartAddress));

        // 添加到表格
        for (MemoryManager.Partition partition : partitions) {
            memoryTableModel.addRow(new Object[]{
                    partition.getStartAddress(),
                    partition.getLength(),
                    partition.getStatus()
            });
        }

        // 更新内存状态
        int allocated = 0;
        int free = 0;
        for (MemoryManager.Partition p : partitions) {
            if ("已分".equals(p.getStatus())) {
                allocated += p.getLength();
            } else {
                free += p.getLength();
            }
        }
        memoryStatusLabel.setText("内存状态: 已分配 " + allocated + "KB, 空闲 " + free + "KB");
        memoryStatusLabel.setForeground(Color.BLUE);

        // 重新验证和重绘面板
        memoryPanel.revalidate();
        memoryPanel.repaint();
    }

    // 添加更新所有表格的辅助方法 (改为public)
    public void updateAllTables() {
        updateRunningProcessTable();
        updateQueueTable("ready", readyQueueTableModel);
        updateQueueTable("reserve", reserveQueueTableModel);
        updateQueueTable("suspend", suspendQueueTableModel);
        updateMemoryTable();
    }

    // Getter方法
    public DefaultTableModel getReadyQueueTableModel() {
        return readyQueueTableModel;
    }

    public DefaultTableModel getReserveQueueTableModel() {
        return reserveQueueTableModel;
    }

    public DefaultTableModel getSuspendQueueTableModel() {
        return suspendQueueTableModel;
    }
}