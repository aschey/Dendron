package DPL;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;

class GraphWriter {
    private int nodeNum;
    private int nullNum;
    private PrintWriter graph;
    private ArrayDeque<Lexeme> queue;
    private Lexeme tree;
    private String title;

    GraphWriter(Lexeme tree, String title) {
        this.nodeNum = 1;
        this.nullNum = 0;
        this.title = title;
        try {
            this.graph = new PrintWriter(this.title + ".dot");
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
        this.queue = new ArrayDeque<>();
        this.tree = tree;
    }

    void showGraph() {
        try {
            Runtime current = Runtime.getRuntime();
            current.exec(String.format("dot %1$s.dot -T eps -o %1$s.eps", this.title));
            current.exec(String.format("evince %s.eps", this.title));
        }
        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    void createGraph() {
        this.graph.write("digraph {\n");
        this.graph.write("graph [ordering=\"out\"];\n");
        this.queue.addFirst(this.tree);
        this.graph.write("Node0 [label=" + this.tree.getVal() + "];\n");
        int localRootNode = 0;
        while (!this.queue.isEmpty()) {
            Lexeme current = this.queue.removeLast();
            String curNode = "Node" + localRootNode;
            this.writeToGraph(curNode, current.left);
            this.writeToGraph(curNode, current.right);
            localRootNode++;
        }
        this.graph.write("}");
        this.graph.close();
    }

    private void writeToGraph(String curNode, Lexeme directionNode) {
        if (directionNode != null) {
            String nextNode = "Node" + this.nodeNum;
            this.nodeNum++;
            this.graph.write(nextNode + " [label=" + directionNode.getVal() + "];\n");
            this.graph.write(curNode + " -> " + nextNode + ";\n");
            this.queue.addFirst(directionNode);
        }
        else {
            String nullStr = "Null" + this.nullNum;
            this.nullNum++;
            this.graph.write(nullStr + " [shape=point];\n");
            this.graph.write(curNode + " -> " + nullStr + ";\n");
        }
    }
}
