package main;

import java.util.LinkedList;
import java.util.List;

public class DFWBlock {
    private SubMatrix target;
    private List<SubMatrix> paths;

    public DFWBlock(SubMatrix target) {
        this.target = target;
        this.paths = new LinkedList<>();
    }

    public List<SubMatrix> getPaths() {
        return paths;
    }

    public void addPath(SubMatrix path){
        this.paths.add(path);
    }
}
