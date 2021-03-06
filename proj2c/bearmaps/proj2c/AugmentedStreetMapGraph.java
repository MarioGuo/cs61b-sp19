package bearmaps.proj2c;

import bearmaps.hw4.streetmap.Node;
import bearmaps.hw4.streetmap.StreetMapGraph;
import bearmaps.proj2ab.Point;
import bearmaps.proj2ab.WeirdPointSet;

import java.lang.reflect.Array;
import java.util.*;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 *
 * @author Alan Yao, Josh Hug, ________
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {
    private List<Point> points;
    private HashMap<Point, Node> map;
    private MyTrieSet trie;
    private HashMap<String, String> cleanToOriginal;
    private HashMap<String, ArrayList> sameName;

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        List<Node> nodes = this.getNodes();
        points = new ArrayList<>();
        map = new HashMap<>();
        trie = new MyTrieSet();
        cleanToOriginal = new HashMap<>();
        sameName = new HashMap<>();
        for (Node n : nodes) {
            if (neighbors(n.id()).size() > 0) {
                Point newPoint = new Point(n.lon(), n.lat());
                points.add(newPoint);
                map.put(newPoint, n);
            }
            if (n.name() != null) {
                String cleanedName = cleanString(n.name());
                trie.add(cleanedName);
                cleanToOriginal.put(cleanedName, n.name());
                ArrayList<Node> newArray;
                if (sameName.containsKey(cleanedName)) {
                    newArray = sameName.get(cleanedName);
                } else {
                    newArray = new ArrayList<>();
                }
                newArray.add(n);
                sameName.put(cleanedName, newArray);
            }
        }
    }


    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        WeirdPointSet kdtree = new WeirdPointSet(points);
        Point result = kdtree.nearest(lon, lat);
        return map.get(result).id();
    }


    /**
     * For Project Part III (gold points)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with or without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        ArrayList<String> original = new ArrayList<>();
        for (String cleaned : trie.keysWithPrefix(prefix)) {
            original.add(cleanToOriginal.get(cleaned));
        }
        return original;
    }

    /**
     * For Project Part III (gold points)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<Node> nodes = sameName.get(cleanString(locationName));
        for (Node n : nodes) {
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("lat", lat(n.id()));
            newMap.put("lon", lon((n.id())));
            newMap.put("name", name(n.id()));
            newMap.put("id", n.id());
            mapList.add(newMap);
        }
        return mapList;
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}
