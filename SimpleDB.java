import java.util.Scanner;
import java.lang.Math;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.StringBuilder;
import java.io.*;

// RadixTree?
class RadixTree {
  public boolean DEBUG = false;

  public Node root;

  public RadixTree() {
    root = new Node();
  }

  // TODO: rename s to key to match insert
  // TODO: rename function to get
  public Node search(Node n, String s) {
    // for each edge
    for(int i = 0; i < n.edges.size(); i++) {
      Edge e = n.edges.get(i);
      if(e.s.equals(s)) {
        if(DEBUG) { System.out.println("found"); }
        return e.n; // key found
      } else {
        for(int j = Math.min(e.s.length(), s.length()); j >= 0; j--) {
          if(DEBUG) { System.out.println("search: " + e.s.substring(0, j) + ":" + (s.substring(0, j))); }
          if(e.s.substring(0, j).equals(s.substring(0, j))) {
            if(e.s.charAt(e.s.length() - 1) != '$') {
              if(DEBUG) { System.out.println("recursive suffix" + s.substring(j + 1, s.length())); }

              return search(e.n, s.substring(j, s.length()));
            }
          }
        }
      }
    }
    if(DEBUG) { System.out.println("not found, edges but not matching"); }
    return null; // not found
  }

  public int insert(Node n, String key, String val) {
    // for each edge
    for(int i = 0; i < n.edges.size(); i++) {
      Edge e = n.edges.get(i);
      if(e.s.equals(key)) {
        // key found
        // can't insert
        return -1;
      } else {
        for(int j = Math.min(e.s.length(), key.length()); j >= 0; j--) {
          if(DEBUG) { System.out.println("insert: " + e.s.substring(0, j) + ":" + (key.substring(0, j))); }
          if(e.s.substring(0, j).equals(key.substring(0, j))) {
            if(e.s.charAt(e.s.length() - 1) != '$') {
              return insert(e.n, key.substring(j, key.length()), val);
            } else {

              // we potentially want to split here
              // but maybe we can specialize a bit more, and split there

              // split
              Node nA = new Node(e.n.val, null);
              e.n.setVal(null);
              if(DEBUG) { System.out.println(n.val); }
              String prefix = e.s.substring(0, j);
              String suffix = e.s.substring(j, e.s.length());
              e.s = prefix;
              Edge eA = new Edge(suffix, nA);
              e.n.edges.add(eA);

              Node nB = new Node(val, null);
              Edge eB = new Edge(key.substring(j, key.length()), nB);
              if(DEBUG) { System.out.println("eB: " + eB); }
              e.n.edges.add(eB);
              if(DEBUG) { System.out.println("split: " + e.n); }

              return 2;
            }
          }
        }
      }
    }
    // key not found
    Node nTemp = new Node(val, null);
    Edge eTemp = new Edge(key, nTemp);
    n.edges.add(eTemp);
    return 3;
    // search as far as possible
    // split into two
    // insert node
  }

  public String toString() {
    Node n = this.root;
    String s = "";
    return toString(n, s);
  }

  public String toString(Node n, String s) {
    if(n.edges.size() == 0) {
      return n.toString();
    } else {
      String t = "";
      t += n.toString() + "\n";
      for(int i = 0; i < n.edges.size(); i++) {
        Edge e = n.edges.get(i);
        t += e.toString() + "\t" + toString(e.n, s);
      }
      return t;
    }
  }

}

class Node {
  // inner nodes have val = null
  public String val;
  public ArrayList<Edge> edges;
  //public Node p;

  public Node() {
    this.val = null;
    this.edges = new ArrayList<Edge>();
    //this.p = null;
  }

  public Node(String val, ArrayList<Edge> edges) {
    this.val = val;
    if(edges == null) {
      this.edges = new ArrayList<Edge>();
    } else {
      this.edges = edges;
    }
    //this.p = p;
  }

  public void setVal(String val) {
    this.val = val;
  }

  public String toString() {
    return "{val: " + val + ", #edges: " + edges.size() + "}";
  }

}

class Edge {
  public String s;
  public Node n;
  
  public Edge(String s, Node n) {
    this.s = s;
    this.n = n;
  }

  public String toString() {
    return "--" + s + "--";
  }

}

public class SimpleDB {
  public static boolean DEBUG = true;

  public static void set(String key, String val) {


  }

  public static void main(String[] args) {
    RadixTree r = new RadixTree();
    System.out.println(r.insert(r.root, "key1$", "val1"));
    System.out.println(r.insert(r.root, "key2$", "val2"));
    //System.out.println("after: " + r.root.edges.get(0).n);

    System.out.println("search 1: " + r.search(r.root, "key1$"));
    System.out.println("search 2: " + r.search(r.root, "key2$"));

    System.out.println("e1: " + r.root.edges.get(0));
    System.out.println("e1,1: " + r.root.edges.get(0).n.edges.get(0));
    System.out.println("e1,2: " + r.root.edges.get(0).n.edges.get(1));

    System.out.println("root:\n" + r);


    //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    //String line = br.readLine();
    //int N = Integer.parseInt(line);
    //for (int i = 1; i < N; i++) {
      //System.out.println("hello world");
    //}
  }
}

/*
 * BEGIN
   * a)
     * take snapshot of radix tree state
     * add to stack
     * potentially faster runtime, takes up more space
   * b)
     * record each command in stack
     * playback opposite of each command backwards
       * there's just SET/UNSET
     * takes up negligible space but is slow to rollback

  * ROLLBACK
  * COMMIT
    * destroy begin stack
*/
