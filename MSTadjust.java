import java.util.*;

class Node<T>{
    T name;
    Node<T> prev;

    public Node (T name, Node<T> prev){
         this.name = name;
         this.prev = prev;
    }

    public String toString(){
        return String.format("(%s, %s)", name.toString(), prev.name.toString());
    }
}

class Edge<T> implements Comparable<Edge<T>>{
    T head;
    T tail;
    Double weight;

    public Edge (T head, T tail, Double weight){
        this.head = head;
        this.tail = tail;
        this.weight = weight;
    }

    public int compareTo(Edge<T> other){
        Double comparison = this.weight - other.weight;
        if (comparison <= 0){
            return (int) Math.floor(comparison);
        }
        else{
            return (int) Math.ceil(comparison);
        }
    }
}



public class MSTadjust {

    public static <T> Node<T> DFS1(Node<T> cycleSource, Node<T> currentNode, Map<T, Set<T>> mst, HashSet<T> nodesFound){
        T nodeName = currentNode.name;
        Set<T> nodeEdges = mst.get(nodeName);
        for (T connectedNodeName : nodeEdges){
            Node<T> newNode = new Node<T> (connectedNodeName, currentNode);
            if (connectedNodeName.equals(cycleSource.name) && !cycleSource.name.equals(currentNode.prev.name)){
                return currentNode;
            }
            else{
                if (!nodesFound.contains(connectedNodeName)){
                    nodesFound.add(connectedNodeName);
                    Node<T> cycleEnd = DFS1(cycleSource, newNode, mst, nodesFound);
                    if (cycleEnd != null){
                        return cycleEnd;
                    }
                }
            }
        }
        return null;
    }

    public static <T> void DFS2 (Map<T, Set<T>> mst, T node, HashSet<T> nodesFound){
        nodesFound.add(node);
        Set<T> nodeEdges = mst.get(node);
        for (T connectedNode : nodeEdges){
            if (!nodesFound.contains(connectedNode)){
                nodesFound.add(connectedNode);
                DFS2(mst, connectedNode, nodesFound);
            }
        }
    }


    public static <T> Map<T, Set<T>> case1(Map<T, Map<T, Double>> graph, Map<T, Set<T>> mst, T head, T tail, double changedWeight) {

        // update graph
        Map<T, Double> newHeadMap = graph.get(head);
        newHeadMap.replace(tail, changedWeight);
        graph.replace(head, newHeadMap);

        Map<T, Double> newTailMap = graph.get(tail);
        newTailMap.replace(head, changedWeight);
        graph.replace(tail, newTailMap);


        // add edge to MST
        Set<T> newHeadSet = mst.get(head);
        newHeadSet.add(tail);
        mst.put(head, newHeadSet);

        Set<T> newTailSet = mst.get(tail);
        newTailSet.add(head);
        mst.put(tail, newTailSet);


        // find all nodes that belong to that new cycle using DFS
        Node<T> cycleSource = new Node<T>(head, null);
        Node<T> cycleEnd = null;

        HashSet<T> nodesFound = new HashSet<T>();
        nodesFound.add(head);
        cycleEnd = DFS1(cycleSource, cycleSource, mst, nodesFound);


        // find heaviest node in cycle
        Node<T> nodeA = cycleSource;
        Node<T> nodeB = cycleEnd;

        // record heaviest
        Double heaviestEdge = -1.0;
        T heaviestA = null;
        T heaviestB = null;

        while (nodeA != null && nodeB != null){
            Double edgeCost = graph.get(nodeA.name).get(nodeB.name);
            if (edgeCost > heaviestEdge){
                heaviestEdge = edgeCost;
                heaviestA = nodeA.name;
                heaviestB = nodeB.name;
            }
            nodeA = nodeB;
            nodeB = nodeB.prev;
        }

        // remove heaviest node in cycle
        Set<T> newASet = mst.get(heaviestA);
        newASet.remove(heaviestB);
        mst.put(heaviestA, newASet);

        Set<T> newBSet = mst.get(heaviestB);
        newBSet.remove(heaviestA);
        mst.put(heaviestB, newBSet);

        // return MST
        return mst;
    }

    public static <T> Map<T, Set<T>> case2(Map<T, Map<T, Double>> graph, Map<T, Set<T>> mst, T head, T tail, double changedWeight) {
        // update graph
        Map<T, Double> tempMapHead = graph.get(head);
        tempMapHead.replace(tail, changedWeight);
        graph.replace(head, tempMapHead);

        Map<T, Double> tempMapTail = graph.get(tail);
        tempMapTail.replace(head, changedWeight);
        graph.replace(tail, tempMapTail);

        // remove edge -> this will result in two disconnected trees
        Set<T> headSet = mst.get(head);
        headSet.remove(tail);
        mst.put(head, headSet);

        Set<T> tailSet = mst.get(tail);
        tailSet.remove(head);
        mst.put(tail, tailSet);

        //run DFS from both nodes that are connected to removed edge to find all nodes apart of their trees
        HashSet<T> tree1 = new HashSet<T>();
        DFS2(mst, head, tree1);

        HashSet<T> tree2 = new HashSet<T>();
        DFS2(mst, tail, tree2);

        //put all edges into a priority queue that are not in the MST
        PriorityQueue<Edge<T>> queue = new PriorityQueue<Edge<T>>();
        for (Map.Entry<T, Map<T, Double>> node1graph : graph.entrySet()) {
            T node1 = node1graph.getKey();
            for(Map.Entry<T, Double> node2graph: node1graph.getValue().entrySet()){
                T node2 = node2graph.getKey();
                if (!mst.get(node1).contains(node2)){
                    Double edgeWeight = graph.get(node1).get(node2);
                    Edge<T> oneEdge = new Edge<T> (node1, node2, edgeWeight);
                    queue.add(oneEdge);
                }
            }
        }

        //pull edges from priority queue until you find one that has one node in tree1 and one node in tree2 -> minimum cost edge -> cut property
        boolean edgeFound = false;
        while (!edgeFound){
            Edge <T> selected = queue.poll();
            if ( (tree1.contains(selected.head) && tree2.contains(selected.tail)) || (tree1.contains(selected.tail) && tree2.contains(selected.head))){

                Set<T> addingHeadSet = mst.get(selected.head);
                addingHeadSet.add(selected.tail);
                mst.put(selected.head, addingHeadSet);

                Set<T> addingTailSet = mst.get(selected.tail);
                addingTailSet.add(selected.head);
                mst.put(selected.tail, addingTailSet);

                edgeFound = true;
            }
        }

        return mst;

    }



    public static <T> Map<T, Set<T>> adjustMST(Map<T, Map<T, Double>> graph, Map<T, Set<T>> mst, T head, T tail, double changedWeight) {

        double oldWeight = graph.get(head).get(tail);
        boolean edgeInMST = false;
        if (mst.get(head).contains(tail)){
            edgeInMST = true;
        }

        //CASE 1: edge is NOT in MST & weight becomes smaller
        if (!edgeInMST && (oldWeight > changedWeight)){
            return case1(graph, mst, head, tail, changedWeight);
        }


        //CASE 2: edge is in MST & weight becomes larger
        if (edgeInMST && (oldWeight < changedWeight)){
            return case2(graph, mst, head, tail, changedWeight);
        }

        //CASE 3: edge is NOT in MST & weight is unchanged or larger
            // do nothing


        //CASE 4: edge is in MST & weight is unchanged or smaller
            // do nothing


        // return the new (possibly unchanged) MST
        return mst;

    }
    public static void main(String[] args){
        //Map<Integer, Map<Integer, Double>> graph = Project6Helper.readGraph("6node.txt");
        //System.out.println(graph);
        //Map<Integer, Set<Integer>> mst = Project6Helper.computeMST(graph);
        //System.out.println(mst);
        //System.out.println(adjustMST(graph, mst, 5, 6, 20.0));


    }

}
