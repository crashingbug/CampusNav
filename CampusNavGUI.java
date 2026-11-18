import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Campus Navigation System - Multi-Window GUI
 *
 * Window 1: Overall Campus Navigation (9 Campus Locations, unweighted & undirected)
 * Window 2: Engineering College Internal Navigation (Multi-Floor Architecture: Ground + Floors 1-5)
 *
 * Pipeline: Graph (Unweighted) + Queue (BFS traversal) + Stack (Backtracking) + Linked List (RouteList)
 * NOTE: All distance and weight properties have been completely removed.
 *
 * Run: javac CampusNav.java CampusNavGUI.java && java CampusNavGUI
 */
public class CampusNavGUI extends JFrame {

    // =========================================================================
    // WINDOW 1: OVERALL CAMPUS MAP PANEL
    // =========================================================================
    static class CampusMapPanel extends JPanel {
        static final Map<String, Point> POS = new LinkedHashMap<>();
        static {
            // Row 1: Top Branch
            POS.put("Stationary 1", new Point(160, 75));
            POS.put("Wow Momo", new Point(360, 75));
            POS.put("Management College", new Point(560, 75));

            // Row 2: Central Hub & Branch
            POS.put("Pharma College", new Point(160, 195));
            POS.put("Hostel", new Point(360, 195));

            // Row 3: Main Spine
            POS.put("Main Gate", new Point(160, 315));
            POS.put("Architecture College", new Point(360, 315));
            POS.put("Engineering College", new Point(560, 315));
            POS.put("Stationary 2", new Point(740, 315));
        }

        static final int NODE_W = 148, NODE_H = 44;

        final CampusNav.Graph graph;
        CampusNav.RouteList currentRoute = null;
        String startNode = "Main Gate";
        String destNode = "Engineering College";
        java.util.function.Consumer<String> onNodeClicked = s -> {};

        CampusMapPanel(CampusNav.Graph graph) {
            this.graph = graph;
            setPreferredSize(new Dimension(840, 380));
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (Map.Entry<String, Point> entry : POS.entrySet()) {
                        Rectangle r = getNodeRect(entry.getValue());
                        if (r.contains(e.getPoint())) {
                            onNodeClicked.accept(entry.getKey());
                            return;
                        }
                    }
                }
            });
        }

        static Rectangle getNodeRect(Point p) {
            return new Rectangle(p.x - NODE_W / 2, p.y - NODE_H / 2, NODE_W, NODE_H);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Set<String> routeEdges = new HashSet<>();
            Set<String> routeNodes = new HashSet<>();
            if (currentRoute != null && currentRoute.size > 0) {
                List<String> list = currentRoute.toList();
                routeNodes.addAll(list);
                for (int i = 0; i + 1 < list.size(); i++) {
                    String a = list.get(i), b = list.get(i + 1);
                    routeEdges.add(a + "|" + b);
                    routeEdges.add(b + "|" + a);
                }
            }

            Color normalEdgeColor = new Color(0xCFD8DC);
            Color routeEdgeColor = new Color(0xE8710A); // Warm Orange

            // 1. Draw Unweighted Undirected Edges
            for (Map.Entry<String, List<String>> entry : graph.getAdj().entrySet()) {
                String u = entry.getKey();
                Point p1 = POS.get(u);
                if (p1 == null) continue;

                for (String v : entry.getValue()) {
                    if (u.compareTo(v) > 0) continue;
                    Point p2 = POS.get(v);
                    if (p2 == null) continue;

                    boolean isHot = routeEdges.contains(u + "|" + v);
                    g.setColor(isHot ? routeEdgeColor : normalEdgeColor);
                    g.setStroke(new BasicStroke(isHot ? 4.5f : 2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }

            // 2. Draw Nodes
            for (Map.Entry<String, Point> entry : POS.entrySet()) {
                String name = entry.getKey();
                Point center = entry.getValue();
                Rectangle r = getNodeRect(center);

                boolean isStart = name.equals(startNode);
                boolean isDest = name.equals(destNode);
                boolean isOnRoute = routeNodes.contains(name);
                boolean isEngCollege = name.equals("Engineering College");

                if (isStart) {
                    g.setColor(new Color(0xD2E3FC));
                } else if (isDest) {
                    g.setColor(new Color(0xCEEAD6));
                } else if (isOnRoute) {
                    g.setColor(new Color(0xFFE0B2));
                } else {
                    g.setColor(new Color(0xF8F9FA));
                }
                g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);

                if (isStart) {
                    g.setColor(new Color(0x1A73E8));
                    g.setStroke(new BasicStroke(3.5f));
                } else if (isDest) {
                    g.setColor(new Color(0x188038));
                    g.setStroke(new BasicStroke(3.5f));
                } else if (isOnRoute) {
                    g.setColor(new Color(0xE8710A));
                    g.setStroke(new BasicStroke(2.5f));
                } else {
                    g.setColor(new Color(0x9AA0A6));
                    g.setStroke(new BasicStroke(1.5f));
                }
                g.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);

                g.setColor(Color.BLACK);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g.getFontMetrics();
                int textW = fm.stringWidth(name);
                int textY = isEngCollege ? r.y + 19 : r.y + (r.height / 2) + 4;
                g.drawString(name, r.x + (r.width - textW) / 2, textY);

                if (isEngCollege) {
                    g.setFont(new Font("SansSerif", Font.BOLD, 9));
                    g.setColor(new Color(0x1A73E8));
                    String sub = "[Inside Map Available]";
                    int subW = g.getFontMetrics().stringWidth(sub);
                    g.drawString(sub, r.x + (r.width - subW) / 2, r.y + 34);
                }
            }

            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(0x5F6368));
            g.drawString("[Blue] Start  |  [Green] Destination  |  [Orange] BFS Route (Linked List)  |  Click building to select", 16, getHeight() - 12);
        }
    }

    // =========================================================================
    // WINDOW 2: ENGINEERING COLLEGE MULTI-FLOOR MAP PANEL
    // =========================================================================
    static class EngineeringMapPanel extends JPanel {
        final CampusNav.Graph graph;
        CampusNav.RouteList currentRoute = null;
        String startRoom = "Reception";
        String destRoom = "506 - H.O.D. IT";
        int currentFloorView = 0; // 0 = Ground, 1 to 5 = Upper Floors
        java.util.function.Consumer<String> onRoomClicked = s -> {};

        // Cached room positions for each floor
        final Map<Integer, Map<String, Point>> floorPositions = new HashMap<>();

        EngineeringMapPanel(CampusNav.Graph graph) {
            this.graph = graph;
            setPreferredSize(new Dimension(840, 420));
            setBackground(Color.WHITE);

            initPositions();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Map<String, Point> posMap = floorPositions.get(currentFloorView);
                    if (posMap == null) return;

                    for (Map.Entry<String, Point> entry : posMap.entrySet()) {
                        Rectangle r = getRoomRect(entry.getKey(), entry.getValue());
                        if (r.contains(e.getPoint())) {
                            onRoomClicked.accept(entry.getKey());
                            return;
                        }
                    }
                }
            });
        }

        private void initPositions() {
            int cx = 410, cy = 205;

            // --- Floor 0 (Ground Floor): Star Circuit around Reception ---
            Map<String, Point> f0 = new LinkedHashMap<>();
            f0.put("Reception", new Point(cx, cy));
            f0.put("Principal's Office", new Point(cx - 240, cy - 110));
            f0.put("Accounts", new Point(cx + 240, cy - 110));
            f0.put("Exam Centre", new Point(cx - 270, cy + 10));
            f0.put("Tinkerer's Lab", new Point(cx + 270, cy + 10));
            f0.put("Canteen", new Point(cx, cy + 125));
            floorPositions.put(0, f0);

            // --- Floors 1 to 5: Circular Loop Corridor Architecture ---
            for (int f = 1; f <= 5; f++) {
                Map<String, Point> fMap = new LinkedHashMap<>();
                List<String> loopNodes = getFloorLoopSequence(f);

                int n = loopNodes.size();
                int rx = 310, ry = 145;

                for (int i = 0; i < n; i++) {
                    String node = loopNodes.get(i);
                    // Distribute around oval corridor
                    double angle = -Math.PI / 2.0 + (i * 2.0 * Math.PI / n);
                    int x = cx + (int) (rx * Math.cos(angle));
                    int y = cy + (int) (ry * Math.sin(angle));
                    fMap.put(node, new Point(x, y));
                }

                // If Floor 2: CA2 is placed inside connected to Amphitheater
                if (f == 2) {
                    Point amphiPt = fMap.get("Amphitheater");
                    if (amphiPt != null) {
                        int cax = amphiPt.x + (int) (0.42 * (cx - amphiPt.x));
                        int cay = amphiPt.y + (int) (0.42 * (cy - amphiPt.y));
                        fMap.put("CA2", new Point(cax, cay));
                    }
                }

                floorPositions.put(f, fMap);
            }
        }

        private List<String> getFloorLoopSequence(int floor) {
            List<String> list = new ArrayList<>();
            // x01 to x11
            for (int i = 1; i <= 11; i++) {
                list.add(CampusNav.getFloorRoomName(floor, i));
            }
            // B-classrooms sequence
            list.add("B" + floor + "1");
            list.add("B" + floor + "3");
            list.add("B" + floor + "2");
            // x12 and x13
            list.add(CampusNav.getFloorRoomName(floor, 12));
            list.add(CampusNav.getFloorRoomName(floor, 13));

            // Floor-specific insertion between x13 and x14
            if (floor == 2) {
                list.add("Amphitheater");
            } else if (floor == 3) {
                list.add("CA3");
            } else if (floor == 4) {
                list.add("CA4");
            }

            // x14 closes loop to x01
            list.add(CampusNav.getFloorRoomName(floor, 14));
            return list;
        }

        Rectangle getRoomRect(String name, Point p) {
            int w = 72;
            int h = 34;

            if (name.equals("Reception") || name.equals("Principal's Office") || name.equals("Tinkerer's Lab")
                    || name.equals("Exam Centre") || name.equals("Accounts") || name.equals("Canteen")) {
                w = 125;
                h = 38;
            } else if (name.contains(" - ")) {
                w = 158;
                h = 38;
            } else if (name.equals("Amphitheater")) {
                w = 105;
                h = 34;
            } else if (name.startsWith("CA") || name.startsWith("B")) {
                w = 60;
                h = 32;
            }

            return new Rectangle(p.x - w / 2, p.y - h / 2, w, h);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Map<String, Point> posMap = floorPositions.get(currentFloorView);
            if (posMap == null) return;

            // Route nodes & edges set
            Set<String> routeEdges = new HashSet<>();
            Set<String> routeNodes = new HashSet<>();
            if (currentRoute != null && currentRoute.size > 0) {
                List<String> list = currentRoute.toList();
                routeNodes.addAll(list);
                for (int i = 0; i + 1 < list.size(); i++) {
                    String a = list.get(i), b = list.get(i + 1);
                    routeEdges.add(a + "|" + b);
                    routeEdges.add(b + "|" + a);
                }
            }

            Color normalEdgeColor = new Color(0xCFD8DC);
            Color routeEdgeColor = new Color(0xE8710A); // Warm Orange

            // 1. Draw Intral-Floor Pathways
            for (Map.Entry<String, Point> entry : posMap.entrySet()) {
                String u = entry.getKey();
                Point p1 = entry.getValue();

                for (String v : graph.getNeighbors(u)) {
                    // Only draw edge if v is also on this floor's map
                    if (!posMap.containsKey(v)) continue;
                    if (u.compareTo(v) > 0) continue; // Draw once

                    Point p2 = posMap.get(v);
                    boolean isHot = routeEdges.contains(u + "|" + v);
                    g.setColor(isHot ? routeEdgeColor : normalEdgeColor);
                    g.setStroke(new BasicStroke(isHot ? 4.5f : 2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }

            // 2. Draw Rooms / Nodes
            for (Map.Entry<String, Point> entry : posMap.entrySet()) {
                String name = entry.getKey();
                Point center = entry.getValue();
                Rectangle r = getRoomRect(name, center);

                boolean isStart = name.equals(startRoom);
                boolean isDest = name.equals(destRoom);
                boolean isOnRoute = routeNodes.contains(name);
                boolean isVerticalShaft = isVerticalNode(name);

                // Background fill
                if (isStart) {
                    g.setColor(new Color(0xD2E3FC));
                } else if (isDest) {
                    g.setColor(new Color(0xCEEAD6));
                } else if (isOnRoute) {
                    g.setColor(new Color(0xFFE0B2));
                } else {
                    g.setColor(new Color(0xF8F9FA));
                }
                g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);

                // Border
                if (isStart) {
                    g.setColor(new Color(0x1A73E8));
                    g.setStroke(new BasicStroke(3.0f));
                } else if (isDest) {
                    g.setColor(new Color(0x188038));
                    g.setStroke(new BasicStroke(3.0f));
                } else if (isOnRoute) {
                    g.setColor(new Color(0xE8710A));
                    g.setStroke(new BasicStroke(2.5f));
                } else {
                    g.setColor(isVerticalShaft ? new Color(0x7E57C2) : new Color(0x9AA0A6));
                    g.setStroke(new BasicStroke(isVerticalShaft ? 2.0f : 1.2f));
                }
                g.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);

                // Text
                g.setColor(Color.BLACK);
                if (name.contains(" - ")) {
                    // Two-line display for department/labeled rooms
                    String[] parts = name.split(" - ", 2);
                    g.setFont(new Font("SansSerif", Font.BOLD, 10));
                    FontMetrics fm1 = g.getFontMetrics();
                    int w1 = fm1.stringWidth(parts[0]);
                    g.drawString(parts[0], r.x + (r.width - w1) / 2, r.y + 14);

                    g.setFont(new Font("SansSerif", Font.PLAIN, 8));
                    FontMetrics fm2 = g.getFontMetrics();
                    String sub = parts[1];
                    if (sub.length() > 22) sub = sub.substring(0, 20) + "..";
                    int w2 = fm2.stringWidth(sub);
                    g.drawString(sub, r.x + (r.width - w2) / 2, r.y + 27);
                } else {
                    g.setFont(new Font("SansSerif", Font.BOLD, 10));
                    FontMetrics fm = g.getFontMetrics();
                    int tw = fm.stringWidth(name);
                    g.drawString(name, r.x + (r.width - tw) / 2, r.y + (r.height / 2) + 4);
                }

                // Vertical shaft indicator badge
                if (isVerticalShaft && !name.contains(" - ")) {
                    g.setFont(new Font("SansSerif", Font.BOLD, 7));
                    g.setColor(new Color(0x5E35B1));
                    String badge = "[Stairs/Lift]";
                    int bw = g.getFontMetrics().stringWidth(badge);
                    g.drawString(badge, r.x + (r.width - bw) / 2, r.y + r.height + 9);
                }
            }

            // Floor Title & Legend
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.setColor(new Color(0x202124));
            String floorTitle = (currentFloorView == 0)
                    ? "GROUND FLOOR (Floor 0) - Star Circuit around Reception"
                    : "FLOOR " + currentFloorView + " - Circular Corridor Architecture";
            g.drawString(floorTitle, 16, 22);

            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(0x5F6368));
            g.drawString("[Blue] Start  |  [Green] Destination  |  [Orange] BFS Route  |  [Purple] Vertical Shaft  |  Click room to select", 16, getHeight() - 10);
        }

        private boolean isVerticalNode(String name) {
            return name.equals("Reception") || name.endsWith("01") || name.contains("05") || name.endsWith("08");
        }
    }

    // =========================================================================
    // WINDOW 2: JFRAME IMPLEMENTATION (ENGINEERING COLLEGE MULTI-FLOOR)
    // =========================================================================
    public static class EngineeringCollegeWindow extends JFrame {
        final CampusNav.Graph engGraph;
        final CampusNav.Navigator navigator;
        final EngineeringMapPanel mapPanel;

        final JComboBox<String> floorFilterBox;
        final JComboBox<String> startBox;
        final JComboBox<String> destBox;
        final JTextArea outputArea;
        final JLabel statusLabel;
        final DefaultListModel<String> queueListModel;
        final JButton[] floorNavButtons = new JButton[6];

        CampusNavGUI campusWindow;

        public EngineeringCollegeWindow(CampusNavGUI campusWindow) {
            super("Engineering College - Multi-Floor Navigation (Window 2)");
            this.campusWindow = campusWindow;
            this.engGraph = CampusNav.createEngineeringGraph();
            this.navigator = new CampusNav.Navigator(engGraph, "Reception");
            this.mapPanel = new EngineeringMapPanel(engGraph);
            this.queueListModel = new DefaultListModel<>();

            setDefaultCloseOperation(HIDE_ON_CLOSE);
            setLayout(new BorderLayout());

            // Floor Filter Dropdown
            String[] filterOptions = {
                    "All Floors",
                    "Ground Floor (0)",
                    "Floor 1",
                    "Floor 2",
                    "Floor 3",
                    "Floor 4",
                    "Floor 5"
            };
            floorFilterBox = new JComboBox<>(filterOptions);

            startBox = new JComboBox<>();
            destBox = new JComboBox<>();
            populateRoomsDropdown(0); // All floors

            startBox.setSelectedItem("Reception");
            destBox.setSelectedItem("506 - H.O.D. IT");

            floorFilterBox.addActionListener(e -> {
                int selected = floorFilterBox.getSelectedIndex();
                populateRoomsDropdown(selected);
                if (selected > 0) {
                    switchFloorView(selected - 1);
                }
            });

            // Wire map clicks to Destination box
            mapPanel.onRoomClicked = room -> {
                destBox.setSelectedItem(room);
                findRoute();
            };

            // Top Header Bar
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(0x0D47A1)); // Dark Navy
            headerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

            JLabel titleLabel = new JLabel("Engineering College - Multi-Floor Indoor Navigation (Floors 0 to 5)");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
            titleLabel.setForeground(Color.WHITE);

            JButton backToCampusBtn = new JButton("View Overall Campus Map (Window 1)");
            backToCampusBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            backToCampusBtn.setBackground(Color.WHITE);
            backToCampusBtn.setForeground(new Color(0x0D47A1));
            backToCampusBtn.setFocusPainted(false);
            backToCampusBtn.addActionListener(e -> {
                if (this.campusWindow != null) {
                    this.campusWindow.setVisible(true);
                    this.campusWindow.toFront();
                    this.campusWindow.requestFocus();
                }
            });

            headerPanel.add(titleLabel, BorderLayout.WEST);
            headerPanel.add(backToCampusBtn, BorderLayout.EAST);

            // Floor Switcher Toolbar (Above map)
            JPanel floorBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
            floorBar.setBackground(new Color(0xECEFF1));
            floorBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xCFD8DC)));

            JLabel floorBarLabel = new JLabel("Floor Map View: ");
            floorBarLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            floorBar.add(floorBarLabel);

            String[] floorBtnLabels = {"Ground Floor", "Floor 1", "Floor 2", "Floor 3", "Floor 4", "Floor 5"};
            for (int i = 0; i < 6; i++) {
                final int f = i;
                JButton btn = new JButton(floorBtnLabels[i]);
                btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
                btn.setFocusPainted(false);
                btn.addActionListener(e -> switchFloorView(f));
                floorNavButtons[i] = btn;
                floorBar.add(btn);
            }
            updateFloorButtonStyles();

            // East Controls Panel
            JPanel controls = new JPanel();
            controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
            controls.setBorder(new EmptyBorder(12, 12, 12, 12));
            controls.setPreferredSize(new Dimension(340, 520));

            controls.add(createHeaderLabel("Filter Rooms by Floor:"));
            controls.add(makeWide(floorFilterBox));
            controls.add(Box.createVerticalStrut(8));

            controls.add(createHeaderLabel("Start Room / Lab:"));
            controls.add(makeWide(startBox));
            controls.add(Box.createVerticalStrut(8));

            controls.add(createHeaderLabel("Destination Room / Lab:"));
            controls.add(makeWide(destBox));
            controls.add(Box.createVerticalStrut(12));

            // Buttons Grid
            JPanel grid = new JPanel(new GridLayout(0, 2, 6, 6));
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);
            grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

            JButton findBtn = new JButton("Find Route (BFS)");
            findBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
            findBtn.setBackground(new Color(0x1A73E8));
            findBtn.setForeground(Color.WHITE);
            findBtn.setFocusPainted(false);
            findBtn.addActionListener(e -> findRoute());

            JButton swapBtn = new JButton("Swap Nodes");
            swapBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            swapBtn.addActionListener(e -> swapRooms());

            JButton backBtn = new JButton("Back (Stack)");
            backBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            backBtn.addActionListener(e -> goBack());

            JButton resetBtn = new JButton("Reset Reception");
            resetBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            resetBtn.addActionListener(e -> resetNav());

            JButton queueBtn = new JButton("Add to Queue");
            queueBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            queueBtn.addActionListener(e -> enqueue());

            JButton procBtn = new JButton("Process Queue");
            procBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            procBtn.addActionListener(e -> processQueue());

            grid.add(findBtn);
            grid.add(swapBtn);
            grid.add(backBtn);
            grid.add(resetBtn);
            grid.add(queueBtn);
            grid.add(procBtn);
            controls.add(grid);

            controls.add(Box.createVerticalStrut(14));
            controls.add(createHeaderLabel("Waiting Internal Requests (Queue FIFO):"));
            JList<String> queueList = new JList<>(queueListModel);
            JScrollPane qs = new JScrollPane(queueList);
            qs.setPreferredSize(new Dimension(300, 100));
            qs.setAlignmentX(Component.LEFT_ALIGNMENT);
            controls.add(qs);

            // Status Label
            statusLabel = new JLabel("Current Location: Reception | Stack History: 0");
            statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            statusLabel.setBorder(new EmptyBorder(8, 14, 8, 14));

            // Output Area
            outputArea = new JTextArea(8, 40);
            outputArea.setEditable(false);
            outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            outputArea.setLineWrap(true);
            outputArea.setWrapStyleWord(true);
            JScrollPane outScroll = new JScrollPane(outputArea);
            outScroll.setBorder(BorderFactory.createTitledBorder("Step-by-Step Linked List Route (Multi-Floor Architecture - Unweighted)"));

            // Assembly
            add(headerPanel, BorderLayout.NORTH);

            JPanel centerContainer = new JPanel(new BorderLayout());
            centerContainer.add(floorBar, BorderLayout.NORTH);
            centerContainer.add(mapPanel, BorderLayout.CENTER);
            centerContainer.add(statusLabel, BorderLayout.SOUTH);

            add(centerContainer, BorderLayout.CENTER);
            add(controls, BorderLayout.EAST);
            add(outScroll, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(null);

            // Initial calculation
            findRoute();
        }

        private void populateRoomsDropdown(int filterIndex) {
            String currentStart = (String) startBox.getSelectedItem();
            String currentDest = (String) destBox.getSelectedItem();

            startBox.removeAllItems();
            destBox.removeAllItems();

            List<String> allRooms = CampusNav.getAllEngineeringRoomsOrdered();
            for (String room : allRooms) {
                if (filterIndex == 0) {
                    // All floors
                    startBox.addItem(room);
                    destBox.addItem(room);
                } else {
                    int targetFloor = filterIndex - 1;
                    if (CampusNav.getFloorOfRoom(room) == targetFloor) {
                        startBox.addItem(room);
                        destBox.addItem(room);
                    }
                }
            }

            if (currentStart != null && containsItem(startBox, currentStart)) {
                startBox.setSelectedItem(currentStart);
            }
            if (currentDest != null && containsItem(destBox, currentDest)) {
                destBox.setSelectedItem(currentDest);
            }
        }

        private boolean containsItem(JComboBox<String> box, String item) {
            for (int i = 0; i < box.getItemCount(); i++) {
                if (Objects.equals(box.getItemAt(i), item)) return true;
            }
            return false;
        }

        void switchFloorView(int floor) {
            mapPanel.currentFloorView = floor;
            updateFloorButtonStyles();
            mapPanel.repaint();
        }

        void updateFloorButtonStyles() {
            Set<Integer> activeFloors = new HashSet<>();
            if (mapPanel.currentRoute != null) {
                for (String node : mapPanel.currentRoute.toList()) {
                    activeFloors.add(CampusNav.getFloorOfRoom(node));
                }
            }

            for (int i = 0; i < 6; i++) {
                JButton btn = floorNavButtons[i];
                boolean isCurrent = (i == mapPanel.currentFloorView);
                boolean onRoute = activeFloors.contains(i);

                if (isCurrent) {
                    btn.setBackground(new Color(0x1A73E8));
                    btn.setForeground(Color.WHITE);
                    btn.setFont(new Font("SansSerif", Font.BOLD, 11));
                } else if (onRoute) {
                    btn.setBackground(new Color(0xFFE0B2));
                    btn.setForeground(new Color(0xE65100));
                    btn.setFont(new Font("SansSerif", Font.BOLD, 11));
                } else {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(Color.BLACK);
                    btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
                }
            }
        }

        public void setCampusWindow(CampusNavGUI campusWindow) {
            this.campusWindow = campusWindow;
        }

        void findRoute() {
            String start = (String) startBox.getSelectedItem();
            String dest = (String) destBox.getSelectedItem();
            if (start == null || dest == null) return;

            mapPanel.startRoom = start;
            mapPanel.destRoom = dest;
            navigator.setCurrent(start);

            CampusNav.RouteList route = engGraph.findShortestPath(start, dest);
            mapPanel.currentRoute = route;

            // Automatically switch map view to destination floor (or start floor)
            int destFloor = CampusNav.getFloorOfRoom(dest);
            switchFloorView(destFloor);

            if (route != null && !start.equals(dest)) {
                navigator.getBackStack().push(start);
                navigator.setCurrent(dest);
            }

            displayRoute("Indoor Shortest Path (BFS)", start, dest, route);
            refreshUI();
        }

        void swapRooms() {
            String s = (String) startBox.getSelectedItem();
            String d = (String) destBox.getSelectedItem();
            startBox.setSelectedItem(d);
            destBox.setSelectedItem(s);
            findRoute();
        }

        void goBack() {
            if (navigator.getBackStack().isEmpty()) {
                outputArea.setText("Stack History is empty. Cannot go back.");
                return;
            }
            String prev = navigator.getBackStack().pop();
            String curr = (String) destBox.getSelectedItem();

            startBox.setSelectedItem(curr);
            destBox.setSelectedItem(prev);
            mapPanel.startRoom = curr;
            mapPanel.destRoom = prev;

            CampusNav.RouteList route = engGraph.findShortestPath(curr, prev);
            mapPanel.currentRoute = route;
            navigator.setCurrent(prev);

            int destFloor = CampusNav.getFloorOfRoom(prev);
            switchFloorView(destFloor);

            displayRoute("Back Navigation (Stack LIFO)", curr, prev, route);
            refreshUI();
        }

        void enqueue() {
            String target = (String) destBox.getSelectedItem();
            if (target != null && engGraph.containsNode(target)) {
                navigator.enqueue(target);
                queueListModel.addElement(target);
                outputArea.append("\n[Enqueued]: " + target + " (Queue Size: " + queueListModel.size() + ")\n");
            }
        }

        void processQueue() {
            if (navigator.getRequestQueue().isEmpty()) {
                outputArea.setText("Request Queue is empty.\n");
                return;
            }
            outputArea.setText("=== PROCESSING INTERNAL REQUEST QUEUE (FIFO) ===\n");
            String curr = navigator.getCurrent();
            while (!navigator.getRequestQueue().isEmpty()) {
                String next = navigator.getRequestQueue().poll();
                CampusNav.RouteList r = engGraph.findShortestPath(curr, next);
                outputArea.append("\nLeg -> " + next + ":\n  Route: " + r + "\n  Stops: " + (r != null ? r.size : 0) + "\n");
                if (r != null) {
                    navigator.getBackStack().push(curr);
                    curr = next;
                    mapPanel.currentRoute = r;
                }
            }
            queueListModel.clear();
            startBox.setSelectedItem(curr);
            navigator.setCurrent(curr);
            refreshUI();
        }

        void resetNav() {
            startBox.setSelectedItem("Reception");
            destBox.setSelectedItem("506 - H.O.D. IT");
            navigator.reset("Reception");
            queueListModel.clear();
            findRoute();
        }

        void displayRoute(String mode, String start, String dest, CampusNav.RouteList route) {
            StringBuilder sb = new StringBuilder();
            sb.append("=================================================================\n");
            sb.append(" ").append(mode.toUpperCase()).append("\n");
            sb.append("=================================================================\n");
            sb.append(" Start Room / Lab  : ").append(start).append(" [Floor ").append(CampusNav.getFloorOfRoom(start)).append("]\n");
            sb.append(" Destination Room  : ").append(dest).append(" [Floor ").append(CampusNav.getFloorOfRoom(dest)).append("]\n\n");

            if (route == null) {
                sb.append(" No indoor route found between these locations.\n");
            } else if (start.equals(dest)) {
                sb.append(" You are already at ").append(start).append("!\n");
            } else {
                sb.append(" Step-by-Step Linked List Route:\n");
                sb.append("   ").append(route.toString()).append("\n\n");

                sb.append(" Individual Stops & Floor Breakdown:\n");
                List<String> nodes = route.toList();
                int lastFloor = -1;
                for (int i = 0; i < nodes.size(); i++) {
                    String node = nodes.get(i);
                    int floor = CampusNav.getFloorOfRoom(node);
                    String floorTag = (floor == 0) ? "Ground Floor" : "Floor " + floor;

                    if (floor != lastFloor) {
                        sb.append("   --- ").append(floorTag).append(" ---\n");
                        lastFloor = floor;
                    }

                    sb.append(String.format("     [%d] %s%s\n", (i + 1), node,
                            i == 0 ? " (Origin)" : i == nodes.size() - 1 ? " (Target Destination)" : ""));
                }

                sb.append("\n Route Summary:\n");
                sb.append("   - Total Stops           : ").append(route.size).append(" rooms/waypoints\n");
                sb.append("   - Corridor Transitions  : ").append(route.hopCount()).append(" hops\n");
                sb.append("   - Start Floor           : Floor ").append(CampusNav.getFloorOfRoom(start)).append("\n");
                sb.append("   - Destination Floor     : Floor ").append(CampusNav.getFloorOfRoom(dest)).append("\n");
                sb.append("   - Algorithm             : Unweighted BFS (Queue) + Backtracking (Stack)\n");
                sb.append("   - Data Storage          : Singly Linked List (RouteList)\n");
            }
            sb.append("=================================================================\n");
            outputArea.setText(sb.toString());
            outputArea.setCaretPosition(0);
        }

        void refreshUI() {
            statusLabel.setText("Current Location: " + navigator.getCurrent() + "  |  Stack History: " + navigator.getBackStack().size());
            updateFloorButtonStyles();
            mapPanel.repaint();
        }
    }

    // =========================================================================
    // WINDOW 1: OVERALL CAMPUS GUI
    // =========================================================================
    final CampusNav.Graph campusGraph;
    final CampusNav.Navigator navigator;
    final CampusMapPanel mapPanel;

    final JComboBox<String> startBox;
    final JComboBox<String> destBox;
    final JTextArea outputArea;
    final JLabel statusLabel;
    final DefaultListModel<String> queueListModel;

    EngineeringCollegeWindow engineeringWindow;

    public CampusNavGUI() {
        super("Campus Navigation System - Overall Campus (Window 1)");
        this.campusGraph = CampusNav.createCampusGraph();
        this.navigator = new CampusNav.Navigator(campusGraph, "Main Gate");
        this.mapPanel = new CampusMapPanel(campusGraph);
        this.queueListModel = new DefaultListModel<>();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Campus dropdowns with exact 9 locations
        String[] campusLocations = {
                "Management College",
                "Wow Momo",
                "Stationary 1",
                "Pharma College",
                "Hostel",
                "Main Gate",
                "Architecture College",
                "Engineering College",
                "Stationary 2"
        };
        startBox = new JComboBox<>(campusLocations);
        destBox = new JComboBox<>(campusLocations);
        startBox.setSelectedItem("Main Gate");
        destBox.setSelectedItem("Engineering College");

        // Wire map clicks to Destination box
        mapPanel.onNodeClicked = node -> {
            destBox.setSelectedItem(node);
            findRoute();
        };

        // Top Header Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0x1565C0)); // Royal Blue
        headerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel titleLabel = new JLabel("Campus Navigation System - Overall Campus Map");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);

        JButton openEngBtn = new JButton("Open Engineering College Inside Map (Window 2)");
        openEngBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        openEngBtn.setBackground(new Color(0xFFF3E0));
        openEngBtn.setForeground(new Color(0xE65100)); // Deep Orange
        openEngBtn.setFocusPainted(false);
        openEngBtn.addActionListener(e -> openEngineeringWindow());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(openEngBtn, BorderLayout.EAST);

        // East Controls Panel
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(new EmptyBorder(12, 12, 12, 12));
        controls.setPreferredSize(new Dimension(330, 480));

        controls.add(createHeaderLabel("Start Location:"));
        controls.add(makeWide(startBox));
        controls.add(Box.createVerticalStrut(8));

        controls.add(createHeaderLabel("Destination:"));
        controls.add(makeWide(destBox));
        controls.add(Box.createVerticalStrut(12));

        // Buttons Grid
        JPanel grid = new JPanel(new GridLayout(0, 2, 6, 6));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JButton findBtn = new JButton("Find Route (BFS)");
        findBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        findBtn.setBackground(new Color(0x1A73E8));
        findBtn.setForeground(Color.WHITE);
        findBtn.setFocusPainted(false);
        findBtn.addActionListener(e -> findRoute());

        JButton swapBtn = new JButton("Swap Nodes");
        swapBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        swapBtn.addActionListener(e -> swapLocations());

        JButton backBtn = new JButton("Back (Stack)");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        backBtn.addActionListener(e -> goBack());

        JButton resetBtn = new JButton("Reset Main Gate");
        resetBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        resetBtn.addActionListener(e -> resetNav());

        JButton queueBtn = new JButton("Add to Queue");
        queueBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        queueBtn.addActionListener(e -> enqueue());

        JButton procBtn = new JButton("Process Queue");
        procBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        procBtn.addActionListener(e -> processQueue());

        grid.add(findBtn);
        grid.add(swapBtn);
        grid.add(backBtn);
        grid.add(resetBtn);
        grid.add(queueBtn);
        grid.add(procBtn);
        controls.add(grid);

        controls.add(Box.createVerticalStrut(12));
        JButton engInsideBtn = new JButton("Navigate Inside Engineering College");
        engInsideBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        engInsideBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        engInsideBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        engInsideBtn.setBackground(new Color(0xE3F2FD));
        engInsideBtn.setForeground(new Color(0x1565C0));
        engInsideBtn.addActionListener(e -> openEngineeringWindow());
        controls.add(engInsideBtn);

        controls.add(Box.createVerticalStrut(14));
        controls.add(createHeaderLabel("Waiting Campus Requests (Queue FIFO):"));
        JList<String> queueList = new JList<>(queueListModel);
        JScrollPane qs = new JScrollPane(queueList);
        qs.setPreferredSize(new Dimension(300, 100));
        qs.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(qs);

        // Status Label
        statusLabel = new JLabel("Current Location: Main Gate | Stack History: 0");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setBorder(new EmptyBorder(8, 14, 8, 14));

        // Output Area
        outputArea = new JTextArea(7, 40);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane outScroll = new JScrollPane(outputArea);
        outScroll.setBorder(BorderFactory.createTitledBorder("Step-by-Step Linked List Route (Zero Distances / Weights)"));

        // Assembly
        add(headerPanel, BorderLayout.NORTH);
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(statusLabel, BorderLayout.NORTH);
        centerContainer.add(mapPanel, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
        add(controls, BorderLayout.EAST);
        add(outScroll, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);

        // Initial route
        findRoute();
    }

    public void setEngineeringWindow(EngineeringCollegeWindow engineeringWindow) {
        this.engineeringWindow = engineeringWindow;
    }

    public void openEngineeringWindow() {
        if (this.engineeringWindow == null) {
            this.engineeringWindow = new EngineeringCollegeWindow(this);
        }
        this.engineeringWindow.setVisible(true);
        this.engineeringWindow.toFront();
        this.engineeringWindow.requestFocus();
    }

    void findRoute() {
        String start = (String) startBox.getSelectedItem();
        String dest = (String) destBox.getSelectedItem();
        if (start == null || dest == null) return;

        mapPanel.startNode = start;
        mapPanel.destNode = dest;
        navigator.setCurrent(start);

        CampusNav.RouteList route = campusGraph.findShortestPath(start, dest);
        mapPanel.currentRoute = route;

        if (route != null && !start.equals(dest)) {
            navigator.getBackStack().push(start);
            navigator.setCurrent(dest);
        }

        displayRoute("Shortest Path (Minimum Hops BFS)", start, dest, route);
        refreshUI();
    }

    void swapLocations() {
        String s = (String) startBox.getSelectedItem();
        String d = (String) destBox.getSelectedItem();
        startBox.setSelectedItem(d);
        destBox.setSelectedItem(s);
        findRoute();
    }

    void goBack() {
        if (navigator.getBackStack().isEmpty()) {
            outputArea.setText("Stack History is empty. Cannot go back.");
            return;
        }
        String prev = navigator.getBackStack().pop();
        String curr = (String) destBox.getSelectedItem();

        startBox.setSelectedItem(curr);
        destBox.setSelectedItem(prev);
        mapPanel.startNode = curr;
        mapPanel.destNode = prev;

        CampusNav.RouteList route = campusGraph.findShortestPath(curr, prev);
        mapPanel.currentRoute = route;
        navigator.setCurrent(prev);

        displayRoute("Back Navigation (Stack LIFO)", curr, prev, route);
        refreshUI();
    }

    void enqueue() {
        String target = (String) destBox.getSelectedItem();
        if (target != null && campusGraph.containsNode(target)) {
            navigator.enqueue(target);
            queueListModel.addElement(target);
            outputArea.append("\n[Enqueued]: " + target + " (Queue Size: " + queueListModel.size() + ")\n");
        }
    }

    void processQueue() {
        if (navigator.getRequestQueue().isEmpty()) {
            outputArea.setText("Request Queue is empty.\n");
            return;
        }
        outputArea.setText("=== PROCESSING CAMPUS REQUEST QUEUE (FIFO) ===\n");
        String curr = navigator.getCurrent();
        while (!navigator.getRequestQueue().isEmpty()) {
            String next = navigator.getRequestQueue().poll();
            CampusNav.RouteList r = campusGraph.findShortestPath(curr, next);
            outputArea.append("\nLeg -> " + next + ":\n  Route: " + r + "\n  Stops: " + (r != null ? r.size : 0) + "\n");
            if (r != null) {
                navigator.getBackStack().push(curr);
                curr = next;
                mapPanel.currentRoute = r;
            }
        }
        queueListModel.clear();
        startBox.setSelectedItem(curr);
        navigator.setCurrent(curr);
        refreshUI();
    }

    void resetNav() {
        startBox.setSelectedItem("Main Gate");
        destBox.setSelectedItem("Engineering College");
        navigator.reset("Main Gate");
        queueListModel.clear();
        findRoute();
    }

    void displayRoute(String mode, String start, String dest, CampusNav.RouteList route) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append(" ").append(mode.toUpperCase()).append("\n");
        sb.append("=================================================================\n");
        sb.append(" Start Location : ").append(start).append("\n");
        sb.append(" Destination    : ").append(dest).append("\n\n");

        if (route == null) {
            sb.append(" No path found between these locations.\n");
        } else if (start.equals(dest)) {
            sb.append(" You are already at ").append(start).append("!\n");
        } else {
            sb.append(" Step-by-Step Linked List Route:\n");
            sb.append("   ").append(route.toString()).append("\n\n");

            sb.append(" Individual Waypoints:\n");
            List<String> nodes = route.toList();
            for (int i = 0; i < nodes.size(); i++) {
                sb.append(String.format("   [%d] %s%s\n", (i + 1), nodes.get(i),
                        i == 0 ? " (Origin)" : i == nodes.size() - 1 ? " (Target)" : ""));
            }

            sb.append("\n Summary:\n");
            sb.append("   - Total Stops   : ").append(route.size).append("\n");
            sb.append("   - Total Hops    : ").append(route.hopCount()).append(" hops\n");
            sb.append("   - Algorithm     : Unweighted BFS (Queue) + Backtracking (Stack)\n");
            sb.append("   - Route Storage : Singly Linked List (RouteList)\n");

            if (dest.equals("Engineering College") || route.contains("Engineering College")) {
                sb.append("\n Note: Engineering College has multi-floor navigation! Click 'Open Engineering College Inside Map' to explore inside rooms & departments.\n");
            }
        }
        sb.append("=================================================================\n");
        outputArea.setText(sb.toString());
        outputArea.setCaretPosition(0);
    }

    void refreshUI() {
        statusLabel.setText("Current Location: " + navigator.getCurrent() + "  |  Stack History: " + navigator.getBackStack().size());
        mapPanel.repaint();
    }

    // =========================================================================
    // HELPER UI METHODS
    // =========================================================================
    static JLabel createHeaderLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    static JComponent makeWide(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return c;
    }

    // =========================================================================
    // MAIN APPLICATION ENTRY POINT
    // =========================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            CampusNavGUI campusWin = new CampusNavGUI();
            EngineeringCollegeWindow engWin = new EngineeringCollegeWindow(campusWin);
            campusWin.setEngineeringWindow(engWin);

            // Position both windows side-by-side
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle bounds = ge.getMaximumWindowBounds();

            int winWidth = Math.min(880, (bounds.width - 60) / 2);
            int winHeight = Math.min(760, bounds.height - 40);

            campusWin.setSize(winWidth, winHeight);
            campusWin.setLocation(bounds.x + 20, bounds.y + 20);

            engWin.setSize(winWidth, winHeight);
            engWin.setLocation(bounds.x + winWidth + 40, bounds.y + 20);

            campusWin.setVisible(true);
            engWin.setVisible(true);
        });
    }
}
