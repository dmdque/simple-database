// test 1
// tests root splitting
    RadixTree r = new RadixTree();
    System.out.println(r.insert(r.root, "key1", "val1"));
    System.out.println(r.insert(r.root, "ley2", "val2"));
    System.out.println(r.search(r.root, "key1"));
    System.out.println(r.search(r.root, "ley2"));

    System.out.println(r.root.edges.get(0).n);
    System.out.println(r.root.edges.get(1).n);

    // test 2
  
    //set("key1", "val1");
    //set("a", "val2");

    //System.out.println("root:\n" + r);
    //System.out.println("get key1: " + get("key1"));
    //System.out.println("get key2: " + get("key2"));


    //unset("key1");

    //set("key3", "val2");

    //System.out.println("root:\n" + r);
    //System.out.println("numEqualRT:\n" + numEqualRT);

    //System.out.println("#val2: " + numEqualTo("val2"));

    // end test 2

    // transaction example test 2
    set("a", "50");
    begin();
    get("a");
    set("a", "60");
    begin();
    unset("a");
    get("a");
    rollback();
    get("a");
    commit();
    get("a");
