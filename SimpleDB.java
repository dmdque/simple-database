import java.io.*;
import java.lang.Math;
import java.lang.StringBuilder;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ArrayDeque;

/*
 * RadixTree or BinarySearchTree?
 * Radix:
   * radix will have better run time
     * O(k), where k is length of key
     * independent of n, which is nice
   * trickier to implement
 * BST:
   * bst is O(logn)
   * good enough for specs of this problem
*/

// RadixTree is more interesting to implement

// This implementation was using '$' to terminate strings (to make them prefix-
// free). But since that limits the possibilities for keys and values, and it's
// given that keys nor values will contain spaces, the terminator was changed to
// ' '.
class RadixTree {
  public boolean DEBUG = false;
  public Node root;

  public RadixTree() {
    root = new Node();
  }

  // gets {key, val}, starting search at Node n
  public Node get(Node n, String key) {
    // for each edge
    for(int i = 0; i < n.edges.size(); i++) {
      Edge e = n.edges.get(i);
      if(e.s.equals(key)) {
        return e.n; // key found
      } else {
        for(int j = Math.min(e.s.length(), key.length()); j > 0; j--) {
          if(e.s.substring(0, j).equals(key.substring(0, j))) {
            if(e.s.charAt(e.s.length() - 1) != ' ') {
              return get(e.n, key.substring(j, key.length()));
            }
          }
        }
      }
    }
    return null; // not found
  }

  // inserts {key, val}, starting search at Node n
  // returns 0 on success
  public int insert(Node n, String key, Object val) {
    // for each edge
    for(int i = 0; i < n.edges.size(); i++) {
      Edge e = n.edges.get(i);
      if(e.s.equals(key)) {
        // key found. can't insert new key. should modify existing key
        e.n.val = val;
        return 0;
      } else {
        for(int j = Math.min(e.s.length(), key.length()); j > 0; j--) {
          if(e.s.substring(0, j).equals(key.substring(0, j))) {
            // we potentially want to split key here
            if(e.s.charAt(e.s.length() - 1) != ' ') {
              // but maybe we can specialize a bit more, and split later
              return insert(e.n, key.substring(j, key.length()), val);
            } else {
              // split node a
              Node nA = new Node(e.n.val, null, null);
              e.n.setVal(null);
              String prefix = e.s.substring(0, j);
              String suffix = e.s.substring(j, e.s.length());
              e.s = prefix;
              Edge eA = new Edge(suffix, nA);
              nA.parentEdge = eA;
              e.n.edges.add(eA);

              // node b
              Node nB = new Node(val, null, null);
              Edge eB = new Edge(key.substring(j, key.length()), nB);
              nB.parentEdge = eB;
              e.n.edges.add(eB);
              return 0;
            }
          }
        }
      }
    }
    // key not found, so we insert here
    Node nTemp = new Node(val, null, null);
    Edge eTemp = new Edge(key, nTemp);
    nTemp.parentEdge = eTemp;
    n.edges.add(eTemp);
    return 0;
  }

  // deletes {key, val}, starting search at Node n
  // returns nonzero for abnormal termination
  public int delete(Node n, String key) {
    // for each edge
    for(int i = 0; i < n.edges.size(); i++) {
      Edge e = n.edges.get(i);
      if(e.s.equals(key)) {
        // key found, so we delete it
        n.edges.remove(i);
        if(n.edges.size() == 1) {
          if(n.parentEdge != null) { // parentEdge is only null when n is root
            // merge
            e = n.edges.get(0);
            n.parentEdge.s += e.s; // append edge.s to parent edge.s
            n.val = e.n.val;
            n.edges.remove(0);
          }
        }
        return 0;
      } else {
        for(int j = Math.min(e.s.length(), key.length()); j > 0; j--) {
          if(e.s.substring(0, j).equals(key.substring(0, j))) {
            if(e.s.charAt(e.s.length() - 1) != ' ') {
              return delete(e.n, key.substring(j, key.length()));
            } else {
              // this block should never actually run
              if(DEBUG) { System.out.println(e.s); }
              if(DEBUG) { System.out.println(key); }
              return 2;
            }
          }
        }
      }
    }
    return 1; // key not found
  }

  public String toString() {
    Node n = this.root;
    String s = "";
    return toStringHelper(n, s);
  }

  // simple/primitive helper text output for debugging purposes
  public String toStringHelper(Node n, String s) {
    if(n.edges.size() == 0) {
      return n.toString();
    } else {
      String t = "";
      for(int i = 0; i < n.edges.size(); i++) {
        Edge e = n.edges.get(i);
        t += e.toString() + "\t" + toStringHelper(e.n, s);
      }
      t += n.toString() + "\n";
      return t;
    }
  }

}

class Node {
  public Object val; // inner nodes have val = null
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
  public static boolean DEBUG = false; // for internal debugging prints

  public static RadixTree r;
  public static RadixTree numEqualRT;
  public static ArrayDeque<String> commandStack;
  public static int blockCount = 0; // keeps track of #open transaction blocks

  public static void set(String key, String val) {
    Node rNode = r.get(r.root, key + " ");
    if(blockCount > 0) {
      if(rNode != null) {
        commandStack.push("SET " + key + " " + rNode.val.toString());
      } else {
        commandStack.push("UNSET " + key);
      }
    }

    // if key already existed, must update numEqualRT as if it were unset
    if(rNode != null) {
      String oldVal = rNode.val.toString();
      Node n = numEqualRT.get(numEqualRT.root, oldVal + " ");
      int count = Integer.parseInt(n.val.toString());
      if(count > 1) {
        n.val = count - 1;
      } else {
        numEqualRT.delete(numEqualRT.root, oldVal + " ");
      }
    } 

    // ensure keys are   terminated?
    // maybe should automatically append
    int err;
    if(key.charAt(key.length() - 1) != ' ') {
      err = r.insert(r.root, key + " ", val);
    } else {
      err = r.insert(r.root, key, val);
    }

    Node n = numEqualRT.get(numEqualRT.root, val + " ");
    if(n != null) {
      int count = Integer.parseInt(n.val.toString());
      n.val = new Integer(count + 1);
    } else {
      numEqualRT.insert(numEqualRT.root, val + " ", new Integer(1));
    }

  }

  public static String get(String key) {
    
    Node n = r.get(r.root, key + " ");
    if(n != null) {
      return n.val.toString();
    } else {
      return "NULL";
    }
  }

  public static void unset(String key) {
    // transaction-related code
    Node rNode = r.get(r.root, key + " ");
    if(blockCount > 0) {
      if(rNode != null) {
        commandStack.push("SET " + key + " " + rNode.val.toString());
      }
      // if there was nothing there before, nothing needs to be done
    }

    String val = r.get(r.root, key + " ").val.toString();

    int err;
    err = r.delete(r.root, key + " ");

    Node n = numEqualRT.get(numEqualRT.root, val + " ");
    if(n != null) {
      int count = Integer.parseInt(n.val.toString());
      if(count > 1) {
        n.val = count - 1;
      } else {
        numEqualRT.delete(numEqualRT.root, val + " ");
      }
    } 
  }

  public static int numEqualTo(String val) {
    // need precomputed radix tree of keys to make it O(logn)
    // need to check for unset AND set overwrite
    Node n = numEqualRT.get(numEqualRT.root, val + " ");
    if(n == null) {
      return 0;
    } else {
      return Integer.parseInt(n.val.toString());
    }
  }

  /*
   * Plan for Transactions
   * ---------------------
   * BEGIN:
     * Two options:
     * a)
       * take snapshot of radix tree state
       * add to stack
       * faster runtime for rollback
       * takes up more space
     * b)
       * record each command's opposite in stack
       * playback opposite of each command backwards
         * only modifying commands are SET/UNSET
       * takes up very little space in comparison
       * slower to rollback

       * SET:
         * opposite: val <- get key
           * then unset or "set key val", depending on if val is null or not
       * UNSET:
         * opposite: val <- get key, then "set key val"
         * don't have to do anything if it was null
    * ROLLBACK:
      * pop stack and execute each "opposite command"
    * COMMIT:
      * destroy begin stack
  */

  // adds "END" command at the beginning of each block, so that rollback only
  // rolls back by one block
  public static void begin() {
    blockCount++;
    commandStack.push("END");
  }

  // processes rollback commands
  // processes set, unset, and "end" (different from commandline END)
  public static void rollback() {
  // increments blockCount
    if(blockCount > 0) {
      // must temporarily disable blockCount to prevent transactions from
      // building up during rollback
      int tempTransactionCount = blockCount;
      blockCount = 0;

      while(commandStack.size() > 0) {
        String line = commandStack.pop();
        String[] tokens = line.split(" ");
        if(tokens[0].equalsIgnoreCase("SET")) {
          set(tokens[1], tokens[2]);
        } else if (tokens[0].equalsIgnoreCase("UNSET")) {
          unset(tokens[1]);
        } else if (tokens[0].equalsIgnoreCase("END")) {
          break;
        } else {
          // just in case
          if(DEBUG) { System.out.println("illegal rollback command"); }
          break;
        }
      }

      blockCount = tempTransactionCount - 1;
    } else {
      System.out.println("NO TRANSACTION");
    }
  }

  // clears blockCount and commandStack
  public static void commit() {
    if(blockCount > 0) {
      blockCount = 0;
      commandStack.clear();
    } else {
      System.out.println("NO TRANSACTION");
    }
  }

  public static void main(String[] args) throws Exception {
    r = new RadixTree();
    numEqualRT = new RadixTree();
    commandStack = new ArrayDeque<String>();

    String prompt = "> ";
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String line = "";

    while(!line.equalsIgnoreCase("end")) {
      System.out.print(prompt);
      line = br.readLine();
      String[] tokens = line.split(" ");
      if(tokens[0].equalsIgnoreCase("SET")) {
        set(tokens[1], tokens[2]);
      } else if (tokens[0].equalsIgnoreCase("GET")) {
        System.out.println(get(tokens[1]));
      } else if (tokens[0].equalsIgnoreCase("UNSET")) {
        unset(tokens[1]);
      } else if (tokens[0].equalsIgnoreCase("NUMEQUALTO")) {
        System.out.println(numEqualTo(tokens[1]));
      } else if (tokens[0].equalsIgnoreCase("BEGIN")) {
        begin();
      } else if (tokens[0].equalsIgnoreCase("ROLLBACK")) {
        rollback();
      } else if (tokens[0].equalsIgnoreCase("COMMIT")) {
        commit();
      } else if (tokens[0].equalsIgnoreCase("PRINT")) {
        System.out.println("root: " + r);
      } else if (tokens[0].equalsIgnoreCase("END")) {
        break;
      } else {
        System.out.println("command \"" + tokens[0] + "\" not recognized");
      }
    }

  }
}

