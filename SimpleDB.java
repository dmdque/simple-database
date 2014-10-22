import java.util.Scanner;
import java.lang.Math;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.StringBuilder;
import java.io.*;
import java.util.ArrayDeque;

// RadixTree?
class RadixTree {
  public boolean DEBUG = false;

  public Node root;

  public RadixTree() {
    root = new Node();
  }

  public Node get(Node n, String key) {
    // for each edge
    for(int i = 0; i < n.edges.size(); i++) {
      Edge e = n.edges.get(i);
      if(e.s.equals(key)) {
        if(DEBUG) { System.out.println("found"); }
        return e.n; // key found
      } else {
        for(int j = Math.min(e.s.length(), key.length()); j > 0; j--) {
          if(DEBUG) { System.out.println("search: " + e.s.substring(0, j) + ":" + (key.substring(0, j))); }
          if(e.s.substring(0, j).equals(key.substring(0, j))) {
            if(e.s.charAt(e.s.length() - 1) != '$') {
              if(DEBUG) { System.out.println("recursive suffix" + key.substring(j + 1, key.length())); }

              return get(e.n, key.substring(j, key.length()));
            }
          }
        }
      }
    }
    if(DEBUG) { System.out.println("not found, edges but not matching"); }
    return null; // not found
  }

  public int insert(Node n, String key, Object val) {
    // for each edge
    for(int i = 0; i < n.edges.size(); i++) {
      Edge e = n.edges.get(i);
      if(e.s.equals(key)) {
        // key found
        // can't insert
        // should modify
        e.n.val = val;
        return 1;
      } else {
        for(int j = Math.min(e.s.length(), key.length()); j > 0; j--) {
          if(DEBUG) { System.out.println("insert: " + e.s.substring(0, j) + ":" + (key.substring(0, j))); }
          if(e.s.substring(0, j).equals(key.substring(0, j))) {
            if(true) { System.out.println("e.s: " + e.s); }
            if(e.s.charAt(e.s.length() - 1) != '$') {
              return insert(e.n, key.substring(j, key.length()), val);
            } else {

              // we potentially want to split here
              // but maybe we can specialize a bit more, and split there

              // split
              Node nA = new Node(e.n.val, null, null);
              e.n.setVal(null);
              if(DEBUG) { System.out.println(n.val); }
              String prefix = e.s.substring(0, j);
              String suffix = e.s.substring(j, e.s.length());
              e.s = prefix;
              Edge eA = new Edge(suffix, nA);
              nA.parentEdge = eA;
              e.n.edges.add(eA);

              Node nB = new Node(val, null, null);
              Edge eB = new Edge(key.substring(j, key.length()), nB);
              nB.parentEdge = eB;
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
    Node nTemp = new Node(val, null, null);
    Edge eTemp = new Edge(key, nTemp);
    nTemp.parentEdge = eTemp;
    n.edges.add(eTemp);
    return 3;
    // search as far as possible
    // split into two
    // insert node
  }

  public int delete(Node n, String key) {
    // for each edge
    for(int i = 0; i < n.edges.size(); i++) {
      Edge e = n.edges.get(i);
      if(e.s.equals(key)) {
        // key found
        // delete it
        n.edges.remove(i);

        if(n.edges.size() == 1) {
          if(n.parentEdge != null) { // this should only happen when n is root
            // merge
            e = n.edges.get(0);
            n.parentEdge.s += e.s; // append edge.s to parent edge.s
            n.val = e.n.val;

            n.edges.remove(0);
          }
        }
        return 1;
      } else {
        for(int j = Math.min(e.s.length(), key.length()); j > 0; j--) {
          if(e.s.substring(0, j).equals(key.substring(0, j))) {
            if(e.s.charAt(e.s.length() - 1) != '$') {
              return delete(e.n, key.substring(j, key.length()));
            } else {
              // not found
              // this should never actually run
              System.out.println(e.s);
              System.out.println(key);
              return -1;
            }
          }
        }
      }
    }
    // key not found
    return -2;
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
      for(int i = 0; i < n.edges.size(); i++) {
        Edge e = n.edges.get(i);
        t += e.toString() + "\t" + toString(e.n, s);
      }
      t += n.toString() + "\n";
      return t;
    }
  }

}

class Node {
  // inner nodes have val = null
  public Object val;
  public ArrayList<Edge> edges;
  public Edge parentEdge;

  public Node() {
    this.val = null;
    this.edges = new ArrayList<Edge>();
    this.parentEdge = null;
  }
  
  public Node(Object val, ArrayList<Edge> edges, Edge parentEdge) {
    this.val = val;
    if(edges == null) {
      this.edges = new ArrayList<Edge>();
    } else {
      this.edges = edges;
    }
    this.parentEdge = parentEdge;
  }

  public void setVal(String val) {
    this.val = val;
  }

  public String toString() {
    return "{val: " + val + ", #edges: " + edges.size() + ", parentEdge: " + parentEdge + "}";
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
  public static RadixTree r;
  public static RadixTree helperR;
  public static int transactionCount = 0;

  public static ArrayDeque<String> commandStack;

  public static void set(String key, String val) {
    Node rNode = r.get(r.root, key + "$");
    if(transactionCount > 0) {
      if(rNode != null) {
        commandStack.push("SET " + key + " " + rNode.val.toString());
      } else {
        commandStack.push("UNSET " + key);
      }
    }

    // if key already existed, must update helperR as if it were unset
    if(rNode != null) {
      String oldVal = rNode.val.toString();
      Node n = helperR.get(helperR.root, oldVal + "$");
      int count = Integer.parseInt(n.val.toString());
      if(count > 1) {
        n.val = count - 1;
      } else {
        helperR.delete(helperR.root, oldVal + "$");
      }
    } 

    // ensure keys are $ terminated?
    // maybe should automatically append
    int err;
    if(key.charAt(key.length() - 1) != '$') {
      err = r.insert(r.root, key + "$", val);
    } else {
      err = r.insert(r.root, key, val);
    }
    if(DEBUG) { System.out.println("errcode: " + err); }

    Node n = helperR.get(helperR.root, val + "$");
    if(n != null) {
      int count = Integer.parseInt(n.val.toString());
      n.val = new Integer(count + 1);
    } else {
      helperR.insert(helperR.root, val + "$", new Integer(1));
    }

  }

  public static String get(String key) {
    
    Node n = r.get(r.root, key + "$");
    if(n != null) {
      return n.val.toString();
    } else {
      return "NULL";
    }
  }

  public static void unset(String key) {
    // transaction-related code
    Node rNode = r.get(r.root, key + "$");
    if(transactionCount > 0) {
      if(rNode != null) {
        commandStack.push("SET " + key + " " + rNode.val.toString());
      }
      // if there was nothing there before, nothing needs to be done
    }

    String val = r.get(r.root, key + "$").val.toString();

    int err;
    err = r.delete(r.root, key + "$");
    if(DEBUG) { System.out.println("errcode: " + err); }

    Node n = helperR.get(helperR.root, val + "$");
    if(n != null) {
      int count = Integer.parseInt(n.val.toString());
      if(count > 1) {
        n.val = count - 1;
      } else {
        helperR.delete(helperR.root, val + "$");
      }
    } 
  }

  public static int numEqualTo(String val) {
    // need precomputed radix tree of keys to make it O(logn)
    // need to check for unset AND set overwrite
    Node n = helperR.get(helperR.root, val + "$");
    if(n == null) {
      return 0;
    } else {
      return Integer.parseInt(n.val.toString());
    }
  }

  /*
   * SET:   0
     * opposite: val <- get key
       * then unset or "set key val", depending on if val is null or not
   * UNSET: 1
     * opposite: val <- get key, then "set key val"
     * if null, then don't store anything
  */
  public static void begin() {
    transactionCount++;
    commandStack.push("END");
    if(DEBUG) { System.out.println(transactionCount); }
  }

  public static void rollback() {
    if(transactionCount > 0) {
      if(DEBUG) { System.out.println("command stack size " + commandStack.size()); }
      // must temporarily disable transactionCount
      int tempTransactionCount = transactionCount;
      transactionCount = 0;

      while(commandStack.size() > 0) {
        String line = commandStack.pop();
        String[] tokens = line.split(" ");
        if(DEBUG) { System.out.println("command: " + line); }
        if(tokens[0].equalsIgnoreCase("SET")) {
          set(tokens[1], tokens[2]);
        } else if (tokens[0].equalsIgnoreCase("UNSET")) {
          unset(tokens[1]);
        } else if (tokens[0].equalsIgnoreCase("END")) {
          break;
        } else {
          if(DEBUG) { System.out.println("error"); }
          break;
        }
      }

      transactionCount = tempTransactionCount - 1;
    } else {
      System.out.println("NO TRANSACTION");
    }
  }

  public static void commit() {
    if(transactionCount > 0) {
      transactionCount = 0;
      commandStack.clear();
    } else {
      System.out.println("NO TRANSACTION");
    }
  }

  public static void main(String[] args) throws Exception {
    r = new RadixTree();
    helperR = new RadixTree();
    commandStack = new ArrayDeque<String>();
    // test 1

    //set("key1", "val1");
    //set("a", "val2");

    //System.out.println("root:\n" + r);
    //System.out.println("get key1: " + get("key1"));
    //System.out.println("get key2: " + get("key2"));


    //unset("key1");

    //set("key3", "val2");

    //System.out.println("root:\n" + r);
    //System.out.println("helperR:\n" + helperR);

    //System.out.println("#val2: " + numEqualTo("val2"));

    // end test 1

    //System.out.println(r.insert(r.root, "key1$", "val1"));
    //System.out.println(r.insert(r.root, "key2$", "val2"));
    ////System.out.println("after: " + r.root.edges.get(0).n);

    //System.out.println("search 1: " + r.get(r.root, "key1$"));
    //System.out.println("search 2: " + r.get(r.root, "key2$"));

    //System.out.println("e1: " + r.root.edges.get(0));
    //System.out.println("e1,1: " + r.root.edges.get(0).n.edges.get(0));
    //System.out.println("e1,2: " + r.root.edges.get(0).n.edges.get(1));

    //System.out.println("root:\n" + r);

    //System.out.println("delete: " + r.delete(r.root, "key1$"));
    //System.out.println("root:\n" + r);
    String prompt = "> ";
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String line = "";
    while(!line.equalsIgnoreCase("end")) {
      System.out.print(prompt);
      line = br.readLine();
      String[] tokens = line.split(" ");
      if(tokens[0].equalsIgnoreCase("SET")) {
        if(DEBUG) { System.out.println("input: " + tokens[0] + tokens[1] + tokens[2]); }
        set(tokens[1], tokens[2]);
        if(DEBUG) { System.out.println("root: " + r); }
      } else if (tokens[0].equalsIgnoreCase("GET")) {
        System.out.println(get(tokens[1]));
      } else if (tokens[0].equalsIgnoreCase("UNSET")) {
        unset(tokens[1]);
      } else if (tokens[0].equalsIgnoreCase("numequalto")) {
        System.out.println(numEqualTo(tokens[1]));
      } else if (tokens[0].equalsIgnoreCase("BEGIN")) {
        begin();
      } else if (tokens[0].equalsIgnoreCase("ROLLBACK")) {
        rollback();
      } else if (tokens[0].equalsIgnoreCase("COMMIT")) {
        commit();
      } else if (tokens[0].equalsIgnoreCase("PRINT")) {
        if(DEBUG) { System.out.println("root: " + r); }
      }

    }
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

     * SET:   0
       * opposite: val <- get key
         * then unset or "set key val", depending on if val is null or not
     * UNSET: 1
       * opposite: val <- get key, then "set key val"

    


  * ROLLBACK
  * COMMIT
    * destroy begin stack
*/
