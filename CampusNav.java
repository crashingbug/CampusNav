import java.util.*;

/**
 * Campus Navigation System - Core Engine
 *
 * Provides unweighted, undirected graph representation, BFS shortest path routing,
 * route history stack, and navigation request queue.
 *
 * Data Structures:
 * - Graph       : Unweighted, undirected adjacency list (bi-directional pathways)
 * - Queue       : BFS traversal for shortest path (minimum hops) + FIFO request queue
 * - Stack       : Backtracking path reconstruction + "Back" history of visited places
 * - Linked List : RouteList storing sequential stops as a singly linked list
 *
 * Compile: javac CampusNav.java
 * Run:     java CampusNav [demo]
 */
public class CampusNav {

    // ================= LINKED LIST (The Route) =================
    /**
     * Singly linked list storing the step-by-step route stops without distance weights.
     */
    public static class RouteList {
        public static class Node {
            public String data;
            public Node next;

            public Node(String data) {
                this.data = data;
                this.next = null;
            }
        }

        public Node head;
        public Node tail;
        public int size;

        public void addLast(String data) {
            Node newNode = new Node(data);
            if (head == null) {
                head = tail = newNode;
            } else {
                tail.next = newNode;
                tail = newNode;
            }
            size++;
        }

        public List<String> toList() {
            List<String> list = new ArrayList<>();
            for (Node cur = head; cur != null; cur = cur.next) {
                list.add(cur.data);
            }
            return list;
        }

        public boolean contains(String val) {
            for (Node cur = head; cur != null; cur = cur.next) {
                if (cur.data.equals(val)) return true;
            }
            return false;
        }

        public int hopCount() {
            return size > 0 ? size - 1 : 0;
        }

        public void append(RouteList other) {
            if (other == null || other.head == null) return;
            Node cur = other.head;
            if (tail != null && cur.data.equals(tail.data)) {
                cur = cur.next;
            }
            for (; cur != null; cur = cur.next) {
                addLast(cur.data);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Node cur = head; cur != null; cur = cur.next) {
                sb.append(cur.data);
                if (cur.next != null) {
                    sb.append(" -> ");
                }
            }
            return sb.toString();
        }
    }

    // ================= UNWEIGHTED UNDIRECTED GRAPH =================
    /**
     * Unweighted, undirected graph using an adjacency list.
     * Edge traversals have uniform cost, so BFS computes the shortest path (minimum hops).
     */
    public static class Graph {
        private final Map<String, List<String>> adj = new LinkedHashMap<>();

        /**
         * Adds an unweighted, bi-directional edge between node a and node b.
         */
        public void addEdge(String a, String b) {
            adj.computeIfAbsent(a, k -> new ArrayList<>());
            adj.computeIfAbsent(b, k -> new ArrayList<>());
            if (!adj.get(a).contains(b)) adj.get(a).add(b);
            if (!adj.get(b).contains(a)) adj.get(b).add(a);
        }

        public Map<String, List<String>> getAdj() {
            return Collections.unmodifiableMap(adj);
        }

        public List<String> getNeighbors(String node) {
            return adj.getOrDefault(node, Collections.emptyList());
        }

        public Set<String> getNodes() {
            return adj.keySet();
        }

        public boolean containsNode(String node) {
            return adj.containsKey(node);
        }

        /**
         * Computes the shortest path (minimum hops) using the pipeline:
         * 1. Queue (BFS traversal)
         * 2. Stack (Path reversal / backtracking from destination to start)
         * 3. Linked List (RouteList populated in forward order)
         *
         * @param from Start location
         * @param to   Destination location
         * @return RouteList of stops, or null if unreachable or not in graph
         */
        public RouteList findShortestPath(String from, String to) {
            if (from == null || to == null) return null;
            if (!adj.containsKey(from) || !adj.containsKey(to)) return null;

            if (from.equals(to)) {
                RouteList singleStop = new RouteList();
                singleStop.addLast(from);
                return singleStop;
            }

            // 1. Breadth-First Search (BFS) using a FIFO Queue
            Queue<String> queue = new ArrayDeque<>();
            Map<String, String> prev = new HashMap<>();
            Set<String> visited = new HashSet<>();

            queue.add(from);
            visited.add(from);

            boolean destinationReached = false;
            while (!queue.isEmpty()) {
                String cur = queue.poll();
                if (cur.equals(to)) {
                    destinationReached = true;
                    break;
                }
                for (String neighbor : adj.getOrDefault(cur, Collections.emptyList())) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        prev.put(neighbor, cur);
                        queue.add(neighbor);
                    }
                }
            }

            if (!destinationReached) {
                return null; // Unreachable
            }

            // 2. Backtrack from destination to start using a LIFO Stack
            Deque<String> stack = new ArrayDeque<>();
            String step = to;
            while (step != null) {
                stack.push(step);
                step = prev.get(step);
            }

            // 3. Pop elements from Stack into Singly Linked List (RouteList)
            RouteList route = new RouteList();
            while (!stack.isEmpty()) {
                route.addLast(stack.pop());
            }

            return route;
        }
    }

    // ================= GRAPH FACTORIES =================
    /**
     * Creates the Overall Campus Graph according to the exact layout specification:
     * - Management College <--> Wow Momo
     * - Wow Momo <--> Stationary 1
     * - Stationary 1 <--> Pharma College
     * - Pharma College <--> Hostel
     * - Pharma College <--> Main Gate
     * - Main Gate <--> Architecture College
     * - Architecture College <--> Engineering College
     * - Engineering College <--> Stationary 2
     */
    public static Graph createCampusGraph() {
        Graph g = new Graph();
        g.addEdge("Management College", "Wow Momo");
        g.addEdge("Wow Momo", "Stationary 1");
        g.addEdge("Stationary 1", "Pharma College");
        g.addEdge("Pharma College", "Hostel");
        g.addEdge("Pharma College", "Main Gate");
        g.addEdge("Main Gate", "Architecture College");
        g.addEdge("Architecture College", "Engineering College");
        g.addEdge("Engineering College", "Stationary 2");
        return g;
    }

    /**
     * Helper to get room label / official name for floor x and room index num.
     */
    public static String getFloorRoomName(int floor, int num) {
        if (floor == 1) {
            if (num == 5) return "105 - H.O.D. Automation and Robotics";
            if (num == 10) return "110 - Library";
        } else if (floor == 2) {
            if (num == 5) return "205 - H.O.D. AI and DS";
        } else if (floor == 3) {
            if (num == 5) return "305 - H.O.D. Computer Engineering";
        } else if (floor == 4) {
            if (num == 5) return "405 - H.O.D. EXTC";
        } else if (floor == 5) {
            if (num == 5) return "505 - H.O.D. Humanities and Applied Sciences";
            if (num == 6) return "506 - H.O.D. IT";
        }
        String prefix = floor + (num < 10 ? "0" : "");
        return prefix + num;
    }

    /**
     * Builds floor x (1 to 5) circular loop with insertions.
     */
    private static void buildFloor(Graph g, int floor) {
        // Base loop consecutive edges: x01 <-> x02 <-> ... <-> x11
        for (int i = 1; i <= 10; i++) {
            g.addEdge(getFloorRoomName(floor, i), getFloorRoomName(floor, i + 1));
        }

        // Shared Insertion on ALL Upper Floors (Floors 1-5):
        // Between x11 and x12: break x11 <-> x12, insert Bx1 <-> Bx3 <-> Bx2
        // Sequence: x11 <-> Bx1 <-> Bx3 <-> Bx2 <-> x12
        String bx1 = "B" + floor + "1";
        String bx3 = "B" + floor + "3";
        String bx2 = "B" + floor + "2";
        g.addEdge(getFloorRoomName(floor, 11), bx1);
        g.addEdge(bx1, bx3);
        g.addEdge(bx3, bx2);
        g.addEdge(bx2, getFloorRoomName(floor, 12));

        // x12 <-> x13
        g.addEdge(getFloorRoomName(floor, 12), getFloorRoomName(floor, 13));

        // Floor-specific connection between x13 and x14:
        if (floor == 2) {
            // Floor 2: Amphitheater sits between (214 and CA2) AND between (CA2 and 213)
            // Connect Amphitheater to 214, CA2, and 213
            g.addEdge("Amphitheater", "214");
            g.addEdge("Amphitheater", "CA2");
            g.addEdge("Amphitheater", "213");
        } else if (floor == 3) {
            // Floor 3: 314 <-> CA3 <-> 313
            g.addEdge(getFloorRoomName(floor, 13), "CA3");
            g.addEdge("CA3", getFloorRoomName(floor, 14));
        } else if (floor == 4) {
            // Floor 4: 414 <-> CA4 <-> 413
            g.addEdge(getFloorRoomName(floor, 13), "CA4");
            g.addEdge("CA4", getFloorRoomName(floor, 14));
        } else {
            // Floors 1 and 5: Standard circular edge x13 <-> x14
            g.addEdge(getFloorRoomName(floor, 13), getFloorRoomName(floor, 14));
        }

        // Close the circular loop: x14 <-> x01
        g.addEdge(getFloorRoomName(floor, 14), getFloorRoomName(floor, 1));
    }

    /**
     * Completely rebuilds the Engineering College sub-map from scratch
     * as an unweighted, undirected Graph using the exact multi-floor specifications:
     * - Ground Floor: 6 locations in a star circuit around Reception
     * - Floors 1 to 5: Circular loop x01..x14 with Bx1/Bx3/Bx2 between x11 & x12
     * - Floor 1: 105 - H.O.D. Automation and Robotics, 110 - Library
     * - Floor 2: 205 - H.O.D. AI and DS, 214 <-> Amphitheater <-> CA2 & Amphitheater <-> 213
     * - Floor 3: 305 - H.O.D. Computer Engineering, 314 <-> CA3 <-> 313
     * - Floor 4: 405 - H.O.D. EXTC, 414 <-> CA4 <-> 413
     * - Floor 5: 505 - H.O.D. Humanities and Applied Sciences, 506 - H.O.D. IT
     * - Vertical connections: Reception to 101, Main Lift/Stairs x05, Secondary Stairs x08, Front Stairs x01
     */
    public static Graph createEngineeringGraph() {
        Graph g = new Graph();

        // 1. Ground Floor (Floor 0) - Star Circuit around Reception
        String reception = "Reception";
        g.addEdge(reception, "Canteen");
        g.addEdge(reception, "Exam Centre");
        g.addEdge(reception, "Tinkerer's Lab");
        g.addEdge(reception, "Accounts");
        g.addEdge(reception, "Principal's Office");

        // 2. Upper Floors (Floors 1 to 5)
        for (int floor = 1; floor <= 5; floor++) {
            buildFloor(g, floor);
        }

        // 3. Inter-Floor Connections (Vertical Shafts / Staircases)
        // Reception (Ground) connected to Floor 1:
        g.addEdge("Reception", "101");
        g.addEdge("Reception", getFloorRoomName(1, 5)); // Main lift/stair shaft at 105

        // Main staircase / lift shaft connecting x05 vertically:
        // 105 <-> 205 <-> 305 <-> 405 <-> 505
        for (int f = 1; f < 5; f++) {
            g.addEdge(getFloorRoomName(f, 5), getFloorRoomName(f + 1, 5));
        }

        // Secondary staircase connecting x08 vertically:
        // 108 <-> 208 <-> 308 <-> 408 <-> 508
        for (int f = 1; f < 5; f++) {
            g.addEdge(f + "08", (f + 1) + "08");
        }

        // Entrance staircase connecting x01 vertically:
        // 101 <-> 201 <-> 301 <-> 401 <-> 501
        for (int f = 1; f < 5; f++) {
            g.addEdge(f + "01", (f + 1) + "01");
        }

        return g;
    }

    /**
     * Resolves queries, numbers or labels to canonical node names in the Engineering Graph.
     */
    public static String resolveEngineeringNode(Graph g, String query) {
        if (query == null) return null;
        String q = query.trim();
        if (g.containsNode(q)) return q;

        // Exact ignore-case match
        for (String node : g.getNodes()) {
            if (node.equalsIgnoreCase(q)) return node;
        }

        // Prefix match (e.g. "506" -> "506 - H.O.D. IT")
        for (String node : g.getNodes()) {
            if (node.startsWith(q + " -") || node.startsWith(q + " ")) return node;
        }

        // Substring match (e.g. "Library" -> "110 - Library")
        for (String node : g.getNodes()) {
            if (node.toLowerCase().contains(q.toLowerCase())) return node;
        }

        return null;
    }

    /**
     * Returns an ordered list of all Engineering College rooms organized by floor.
     */
    public static List<String> getAllEngineeringRoomsOrdered() {
        List<String> list = new ArrayList<>();

        // Ground Floor
        list.add("Reception");
        list.add("Canteen");
        list.add("Exam Centre");
        list.add("Tinkerer's Lab");
        list.add("Accounts");
        list.add("Principal's Office");

        // Floors 1 to 5
        for (int f = 1; f <= 5; f++) {
            for (int i = 1; i <= 11; i++) {
                list.add(getFloorRoomName(f, i));
            }
            list.add("B" + f + "1");
            list.add("B" + f + "3");
            list.add("B" + f + "2");
            list.add(getFloorRoomName(f, 12));
            list.add(getFloorRoomName(f, 13));
            if (f == 2) {
                list.add("Amphitheater");
                list.add("CA2");
            } else if (f == 3) {
                list.add("CA3");
            } else if (f == 4) {
                list.add("CA4");
            }
            list.add(getFloorRoomName(f, 14));
        }

        return list;
    }

    /**
     * Determines which floor (0 to 5) a room belongs to.
     */
    public static int getFloorOfRoom(String room) {
        if (room == null) return 0;
        if (room.equals("Reception") || room.equals("Canteen") || room.equals("Exam Centre")
                || room.equals("Tinkerer's Lab") || room.equals("Accounts") || room.equals("Principal's Office")) {
            return 0;
        }
        if (room.equals("CA2") || room.equals("Amphitheater")) return 2;
        if (room.equals("CA3")) return 3;
        if (room.equals("CA4")) return 4;
        if (room.startsWith("B") && room.length() >= 2 && Character.isDigit(room.charAt(1))) {
            return Character.getNumericValue(room.charAt(1));
        }
        if (room.length() >= 1 && Character.isDigit(room.charAt(0))) {
            int f = Character.getNumericValue(room.charAt(0));
            if (f >= 1 && f <= 5) return f;
        }
        return 0;
    }

    // ================= NAVIGATION CONTROLLER =================
    /**
     * Manages current location, Stack-based history ("Back"), and Queue-based pending requests.
     */
    public static class Navigator {
        private final Graph graph;
        private String current;
        private final Deque<String> backStack = new ArrayDeque<>();
        private final Queue<String> requestQueue = new ArrayDeque<>();

        public Navigator(Graph graph, String startLocation) {
            this.graph = graph;
            this.current = startLocation;
        }

        public String getCurrent() { return current; }
        public void setCurrent(String loc) { this.current = loc; }
        public Graph getGraph() { return graph; }
        public Deque<String> getBackStack() { return backStack; }
        public Queue<String> getRequestQueue() { return requestQueue; }

        public RouteList navigateTo(String destination) {
            if (!graph.containsNode(destination)) return null;
            if (destination.equals(current)) {
                RouteList r = new RouteList();
                r.addLast(current);
                return r;
            }
            RouteList route = graph.findShortestPath(current, destination);
            if (route != null) {
                backStack.push(current);
                current = destination;
            }
            return route;
        }

        public RouteList goBack() {
            if (backStack.isEmpty()) return null;
            String previous = backStack.pop();
            RouteList route = graph.findShortestPath(current, previous);
            if (route != null) {
                current = previous;
            }
            return route;
        }

        public boolean enqueue(String destination) {
            if (graph.containsNode(destination)) {
                requestQueue.add(destination);
                return true;
            }
            return false;
        }

        public List<RouteList> processQueue() {
            List<RouteList> completed = new ArrayList<>();
            while (!requestQueue.isEmpty()) {
                String dest = requestQueue.poll();
                RouteList r = navigateTo(dest);
                if (r != null) completed.add(r);
            }
            return completed;
        }

        public void reset(String defaultLocation) {
            this.current = defaultLocation;
            this.backStack.clear();
            this.requestQueue.clear();
        }
    }

    // ================= CONSOLE DEMO & CLI =================
    public static void runDemo() {
        System.out.println("=================================================");
        System.out.println(" CAMPUS NAVIGATION SYSTEM - ALGORITHM DEMO");
        System.out.println("=================================================");

        // 1. Overall Campus Navigation Demo
        System.out.println("\n--- [WINDOW 1: OVERALL CAMPUS DEMO] ---");
        Graph campusGraph = createCampusGraph();
        Navigator campusNav = new Navigator(campusGraph, "Main Gate");
        System.out.println("Initial Location: " + campusNav.getCurrent());

        System.out.println("\n1. Navigating: Main Gate -> Engineering College");
        RouteList r1 = campusNav.navigateTo("Engineering College");
        System.out.println("   Linked List Route : " + r1);
        System.out.println("   Stops Count       : " + r1.size + " stops (" + r1.hopCount() + " hops)");

        System.out.println("\n2. Navigating: Engineering College -> Stationary 2");
        RouteList r2 = campusNav.navigateTo("Stationary 2");
        System.out.println("   Linked List Route : " + r2);
        System.out.println("   Stops Count       : " + r2.size + " stops (" + r2.hopCount() + " hops)");

        System.out.println("\n3. Testing 'Back' Navigation (Stack)");
        RouteList rBack = campusNav.goBack();
        System.out.println("   Returned to       : " + campusNav.getCurrent());
        System.out.println("   Back Route        : " + rBack);

        // 2. Engineering College Multi-Floor Demo
        System.out.println("\n--- [WINDOW 2: ENGINEERING COLLEGE MULTI-FLOOR DEMO] ---");
        Graph engGraph = createEngineeringGraph();
        Navigator engNav = new Navigator(engGraph, "Reception");
        System.out.println("Total Engineering College Nodes: " + engGraph.getNodes().size());

        System.out.println("\n1. Ground Floor Star Circuit: Canteen -> Principal's Office");
        RouteList eg1 = engGraph.findShortestPath("Canteen", "Principal's Office");
        System.out.println("   Route : " + eg1);
        System.out.println("   Hops  : " + eg1.hopCount());

        System.out.println("\n2. Floor 1 B-Classroom Sequence: 111 -> 112");
        RouteList eg2 = engGraph.findShortestPath("111", "112");
        System.out.println("   Route : " + eg2);
        System.out.println("   Hops  : " + eg2.hopCount());

        System.out.println("\n3. Floor 2 Amphitheater / CA2: 214 -> 213");
        RouteList eg3 = engGraph.findShortestPath("214", "213");
        System.out.println("   Route : " + eg3);
        System.out.println("   Hops  : " + eg3.hopCount());

        System.out.println("\n4. Floor 3 CA3 Insertion: 314 -> 313");
        RouteList eg4 = engGraph.findShortestPath("314", "313");
        System.out.println("   Route : " + eg4);
        System.out.println("   Hops  : " + eg4.hopCount());

        System.out.println("\n5. Floor 4 CA4 Insertion: 414 -> 413");
        RouteList eg5 = engGraph.findShortestPath("414", "413");
        System.out.println("   Route : " + eg5);
        System.out.println("   Hops  : " + eg5.hopCount());

        System.out.println("\n6. Cross-Floor Route: Canteen -> 506 - H.O.D. IT");
        RouteList eg6 = engGraph.findShortestPath("Canteen", "506 - H.O.D. IT");
        System.out.println("   Route : " + eg6);
        System.out.println("   Stops : " + eg6.size + " (" + eg6.hopCount() + " hops)");

        System.out.println("\n=================================================");
        System.out.println(" DEMO COMPLETED SUCCESSFULLY (0 WEIGHTS / DISTANCES)");
        System.out.println("=================================================");
    }

    public static void main(String[] args) {
        runDemo();
    }
}
