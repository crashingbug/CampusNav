import java.util.*;

/**
 * Automated Verification Suite for Multi-Floor Campus & Engineering College Navigation System
 */
public class VerificationTest {

    public static void main(String[] args) {
        System.out.println(">>> RUNNING MULTI-FLOOR VERIFICATION SUITE...");

        testCampusTopology();
        testGroundFloorStarCircuit();
        testUpperFloorsSharedInsertions();
        testFloorSpecificRoomsAndInsertions();
        testVerticalAndCrossFloorRouting();
        testNavigationStackAndQueue();
        testGuiDataIntegrity();

        System.out.println("\n>>> ALL MULTI-FLOOR VERIFICATION TESTS PASSED SUCCESSFULLY! (100% OK)");
    }

    static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            System.err.println("TEST FAILED: " + msg);
            throw new AssertionError("Assertion failed: " + msg);
        } else {
            System.out.println("  [PASS] " + msg);
        }
    }

    static void assertEquals(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            System.err.println("TEST FAILED: " + msg + " (Expected: " + expected + ", Got: " + actual + ")");
            throw new AssertionError("Assertion failed: " + msg);
        } else {
            System.out.println("  [PASS] " + msg + " => " + actual);
        }
    }

    static void testCampusTopology() {
        System.out.println("\n--- 1. Testing Overall Campus Graph Topology ---");
        CampusNav.Graph g = CampusNav.createCampusGraph();

        Set<String> expectedNodes = new TreeSet<>(Arrays.asList(
                "Management College",
                "Wow Momo",
                "Stationary 1",
                "Pharma College",
                "Hostel",
                "Main Gate",
                "Architecture College",
                "Engineering College",
                "Stationary 2"
        ));
        assertEquals(expectedNodes, new TreeSet<>(g.getNodes()), "All 9 exact campus nodes present");

        CampusNav.RouteList r = g.findShortestPath("Main Gate", "Engineering College");
        assertEquals("Main Gate -> Architecture College -> Engineering College", r.toString(), "Main Gate to Engineering College route");
    }

    static void testGroundFloorStarCircuit() {
        System.out.println("\n--- 2. Testing Ground Floor (Floor 0) Star Circuit ---");
        CampusNav.Graph eng = CampusNav.createEngineeringGraph();

        // Verify Ground Floor 6 locations
        String[] groundNodes = {
                "Reception", "Canteen", "Exam Centre", "Tinkerer's Lab", "Accounts", "Principal's Office"
        };
        for (String node : groundNodes) {
            assertTrue(eng.containsNode(node), "Ground floor contains: " + node);
            assertEquals(0, CampusNav.getFloorOfRoom(node), node + " belongs to Floor 0");
        }

        // Verify NO numbered rooms on Ground Floor (e.g. 001, 002)
        for (String node : eng.getNodes()) {
            if (node.startsWith("00") || node.startsWith("01")) {
                throw new AssertionError("Found numbered room on Ground Floor: " + node);
            }
        }
        assertTrue(true, "No numbered rooms exist on Ground Floor");

        // Verify Star circuit around Reception
        List<String> receptionNeighbors = eng.getNeighbors("Reception");
        assertTrue(receptionNeighbors.contains("Canteen"), "Reception connects to Canteen");
        assertTrue(receptionNeighbors.contains("Exam Centre"), "Reception connects to Exam Centre");
        assertTrue(receptionNeighbors.contains("Tinkerer's Lab"), "Reception connects to Tinkerer's Lab");
        assertTrue(receptionNeighbors.contains("Accounts"), "Reception connects to Accounts");
        assertTrue(receptionNeighbors.contains("Principal's Office"), "Reception connects to Principal's Office");

        // Verify path from Canteen to Principal's Office goes through Reception
        CampusNav.RouteList starRoute = eng.findShortestPath("Canteen", "Principal's Office");
        assertEquals("Canteen -> Reception -> Principal's Office", starRoute.toString(), "Star circuit route via Reception");
        assertEquals(2, starRoute.hopCount(), "2 hops for star circuit navigation");
    }

    static void testUpperFloorsSharedInsertions() {
        System.out.println("\n--- 3. Testing Upper Floors Shared Insertions (Bx1 <-> Bx3 <-> Bx2) ---");
        CampusNav.Graph eng = CampusNav.createEngineeringGraph();

        for (int f = 1; f <= 5; f++) {
            String x11 = CampusNav.getFloorRoomName(f, 11);
            String x12 = CampusNav.getFloorRoomName(f, 12);
            String bx1 = "B" + f + "1";
            String bx3 = "B" + f + "3";
            String bx2 = "B" + f + "2";

            // Verify direct x11 <-> x12 edge is broken
            assertTrue(!eng.getNeighbors(x11).contains(x12), "Floor " + f + ": direct " + x11 + " <-> " + x12 + " is broken");

            // Verify exact sequence: x11 <-> Bx1 <-> Bx3 <-> Bx2 <-> x12
            assertTrue(eng.getNeighbors(x11).contains(bx1), "Floor " + f + ": " + x11 + " connects to " + bx1);
            assertTrue(eng.getNeighbors(bx1).contains(bx3), "Floor " + f + ": " + bx1 + " connects to " + bx3);
            assertTrue(eng.getNeighbors(bx3).contains(bx2), "Floor " + f + ": " + bx3 + " connects to " + bx2);
            assertTrue(eng.getNeighbors(bx2).contains(x12), "Floor " + f + ": " + bx2 + " connects to " + x12);

            // Test pathfinding between x11 and x12
            CampusNav.RouteList r = eng.findShortestPath(x11, x12);
            String expected = x11 + " -> " + bx1 + " -> " + bx3 + " -> " + bx2 + " -> " + x12;
            assertEquals(expected, r.toString(), "Floor " + f + " path between " + x11 + " and " + x12);
            assertEquals(4, r.hopCount(), "Floor " + f + " hops: 4");
        }
    }

    static void testFloorSpecificRoomsAndInsertions() {
        System.out.println("\n--- 4. Testing Floor-Specific Rooms, Labels & Insertions ---");
        CampusNav.Graph eng = CampusNav.createEngineeringGraph();

        // Floor 1
        assertTrue(eng.containsNode("105 - H.O.D. Automation and Robotics"), "Floor 1 has '105 - H.O.D. Automation and Robotics'");
        assertTrue(eng.containsNode("110 - Library"), "Floor 1 has '110 - Library'");
        assertTrue(eng.getNeighbors("114").contains("113"), "Floor 1 has standard 114 <-> 113 edge");

        // Floor 2
        assertTrue(eng.containsNode("205 - H.O.D. AI and DS"), "Floor 2 has '205 - H.O.D. AI and DS'");
        assertTrue(!eng.getNeighbors("214").contains("213"), "Floor 2 direct 214 <-> 213 edge is broken");
        assertTrue(eng.getNeighbors("Amphitheater").contains("214"), "Amphitheater connects to 214");
        assertTrue(eng.getNeighbors("Amphitheater").contains("CA2"), "Amphitheater connects to CA2");
        assertTrue(eng.getNeighbors("Amphitheater").contains("213"), "Amphitheater connects to 213");

        CampusNav.RouteList r214_213 = eng.findShortestPath("214", "213");
        assertEquals("214 -> Amphitheater -> 213", r214_213.toString(), "214 to 213 route via Amphitheater");

        CampusNav.RouteList r214_ca2 = eng.findShortestPath("214", "CA2");
        assertEquals("214 -> Amphitheater -> CA2", r214_ca2.toString(), "214 to CA2 route via Amphitheater");

        // Floor 3
        assertTrue(eng.containsNode("305 - H.O.D. Computer Engineering"), "Floor 3 has '305 - H.O.D. Computer Engineering'");
        assertTrue(!eng.getNeighbors("314").contains("313"), "Floor 3 direct 314 <-> 313 edge is broken");
        assertTrue(eng.getNeighbors("314").contains("CA3"), "Floor 3: 314 connects to CA3");
        assertTrue(eng.getNeighbors("CA3").contains("313"), "Floor 3: CA3 connects to 313");

        CampusNav.RouteList r314_313 = eng.findShortestPath("314", "313");
        assertEquals("314 -> CA3 -> 313", r314_313.toString(), "314 to 313 route via CA3");

        // Floor 4
        assertTrue(eng.containsNode("405 - H.O.D. EXTC"), "Floor 4 has '405 - H.O.D. EXTC'");
        assertTrue(!eng.getNeighbors("414").contains("413"), "Floor 4 direct 414 <-> 413 edge is broken");
        assertTrue(eng.getNeighbors("414").contains("CA4"), "Floor 4: 414 connects to CA4");
        assertTrue(eng.getNeighbors("CA4").contains("413"), "Floor 4: CA4 connects to 413");

        CampusNav.RouteList r414_413 = eng.findShortestPath("414", "413");
        assertEquals("414 -> CA4 -> 413", r414_413.toString(), "414 to 413 route via CA4");

        // Floor 5
        assertTrue(eng.containsNode("505 - H.O.D. Humanities and Applied Sciences"), "Floor 5 has '505 - H.O.D. Humanities and Applied Sciences'");
        assertTrue(eng.containsNode("506 - H.O.D. IT"), "Floor 5 has '506 - H.O.D. IT'");
        assertTrue(eng.getNeighbors("514").contains("513"), "Floor 5 has standard 514 <-> 513 edge");
    }

    static void testVerticalAndCrossFloorRouting() {
        System.out.println("\n--- 5. Testing Vertical Staircases & Cross-Floor Routing ---");
        CampusNav.Graph eng = CampusNav.createEngineeringGraph();

        // Cross-floor route: Canteen (Floor 0) to 506 - H.O.D. IT (Floor 5)
        CampusNav.RouteList r = eng.findShortestPath("Canteen", "506 - H.O.D. IT");
        String expected = "Canteen -> Reception -> 105 - H.O.D. Automation and Robotics -> 205 - H.O.D. AI and DS -> 305 - H.O.D. Computer Engineering -> 405 - H.O.D. EXTC -> 505 - H.O.D. Humanities and Applied Sciences -> 506 - H.O.D. IT";
        assertEquals(expected, r.toString(), "Cross-floor route: Canteen to 506 - H.O.D. IT");
        assertEquals(8, r.size, "8 stops across 6 floors");
        assertEquals(7, r.hopCount(), "7 hops across 6 floors");

        // Cross-floor route: 110 - Library to 405 - H.O.D. EXTC
        CampusNav.RouteList rLib = eng.findShortestPath("110 - Library", "405 - H.O.D. EXTC");
        assertTrue(rLib != null && rLib.size > 0, "Route found from Library to H.O.D. EXTC");
        assertEquals("110 - Library", rLib.head.data, "Route starts at 110 - Library");
        assertEquals("405 - H.O.D. EXTC", rLib.tail.data, "Route ends at 405 - H.O.D. EXTC");
        System.out.println("  Library to EXTC: " + rLib + " (" + rLib.hopCount() + " hops)");
    }

    static void testNavigationStackAndQueue() {
        System.out.println("\n--- 6. Testing Navigator Stack (Back) & Queue (FIFO) ---");
        CampusNav.Graph eng = CampusNav.createEngineeringGraph();
        CampusNav.Navigator nav = new CampusNav.Navigator(eng, "Reception");

        nav.navigateTo("Canteen");
        assertEquals("Canteen", nav.getCurrent(), "Current is Canteen");
        assertEquals(1, nav.getBackStack().size(), "BackStack has 1 entry");

        nav.navigateTo("Exam Centre");
        assertEquals("Exam Centre", nav.getCurrent(), "Current is Exam Centre");
        assertEquals(2, nav.getBackStack().size(), "BackStack has 2 entries");

        nav.goBack();
        assertEquals("Canteen", nav.getCurrent(), "Went back to Canteen");

        nav.goBack();
        assertEquals("Reception", nav.getCurrent(), "Went back to Reception");

        // FIFO Queue
        nav.enqueue("Accounts");
        nav.enqueue("Tinkerer's Lab");
        assertEquals(2, nav.getRequestQueue().size(), "Queue has 2 items");

        List<CampusNav.RouteList> list = nav.processQueue();
        assertEquals(2, list.size(), "Processed 2 requests");
        assertEquals("Tinkerer's Lab", nav.getCurrent(), "Final location is Tinkerer's Lab");
    }

    static void testGuiDataIntegrity() {
        System.out.println("\n--- 7. Testing GUI Windows Integration ---");
        try {
            CampusNavGUI campusWin = new CampusNavGUI();
            CampusNavGUI.EngineeringCollegeWindow engWin = new CampusNavGUI.EngineeringCollegeWindow(campusWin);
            campusWin.setEngineeringWindow(engWin);
            engWin.setCampusWindow(campusWin);

            assertTrue(engWin.engGraph.containsNode("Reception"), "GUI has Reception");
            assertTrue(engWin.engGraph.containsNode("506 - H.O.D. IT"), "GUI has 506 - H.O.D. IT");
            assertTrue(engWin.engGraph.containsNode("Amphitheater"), "GUI has Amphitheater");
            assertTrue(engWin.engGraph.containsNode("CA2"), "GUI has CA2");
            assertTrue(engWin.engGraph.containsNode("CA3"), "GUI has CA3");
            assertTrue(engWin.engGraph.containsNode("CA4"), "GUI has CA4");
            assertTrue(engWin.engGraph.containsNode("B11"), "GUI has B11");

            assertEquals(95, engWin.startBox.getItemCount(), "Start dropdown contains all 95 rooms");
            assertEquals(95, engWin.destBox.getItemCount(), "Dest dropdown contains all 95 rooms");

            campusWin.dispose();
            engWin.dispose();
            System.out.println("  [PASS] GUI Windows initialized, linked, and verified successfully");
        } catch (java.awt.HeadlessException e) {
            System.out.println("  [SKIP] Headless environment detected");
        }
    }
}
