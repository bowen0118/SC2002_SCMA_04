package edu.ntu.ccds.sc2002;

import edu.ntu.ccds.sc2002.cli.Menu;

public class Main {
    public static void main(String[] args){
        // Default data directory is ./data relative to project root
        String dataPath = args.length>0 ? args[0] : "data";
        new Menu(dataPath).run();
    }
}
