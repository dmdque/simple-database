// test 1
// tests root splitting
    RadixTree r = new RadixTree();
    System.out.println(r.insert(r.root, "key1", "val1"));
    System.out.println(r.insert(r.root, "ley2", "val2"));
    System.out.println(r.search(r.root, "key1"));
    System.out.println(r.search(r.root, "ley2"));

    System.out.println(r.root.edges.get(0).n);
    System.out.println(r.root.edges.get(1).n);
