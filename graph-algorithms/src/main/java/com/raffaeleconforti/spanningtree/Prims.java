package com.raffaeleconforti.spanningtree;

public class Prims {
    private boolean unsettled[];
    private boolean settled[];
    private int numberofvertices;
    private int adjacencyMatrix[][];
    private int key[];
    public static final int INFINITE = 999;
    private int parent[];

    public Prims(int numberofvertices)
    {
        this.numberofvertices = numberofvertices;
        unsettled = new boolean[numberofvertices + 1];
        settled = new boolean[numberofvertices + 1];
        adjacencyMatrix = new int[numberofvertices + 1][numberofvertices + 1];
        key = new int[numberofvertices + 1];
        parent = new int[numberofvertices + 1];
    }

    public int getUnsettledCount(boolean unsettled[])
    {
        int count = 0;
        for (int index = 0; index < unsettled.length; index++)
        {
            if (unsettled[index])
            {
                count++;
            }
        }
        return count;
    }

    public void primsAlgorithm(int adjacencyMatrix[][])
    {
        int evaluationVertex;
        for (int source = 1; source <= numberofvertices; source++)
        {
            System.arraycopy(adjacencyMatrix[source], 1, this.adjacencyMatrix[source], 1, numberofvertices);
        }

        for (int index = 1; index <= numberofvertices; index++)
        {
            key[index] = INFINITE;
        }
        key[1] = 0;
        unsettled[1] = true;
        parent[1] = 1;

        while (getUnsettledCount(unsettled) != 0)
        {
            evaluationVertex = getMimumKeyVertexFromUnsettled(unsettled);
            unsettled[evaluationVertex] = false;
            settled[evaluationVertex] = true;
            evaluateNeighbours(evaluationVertex);
        }
    }

    private int getMimumKeyVertexFromUnsettled(boolean[] unsettled2)
    {
        int min = Integer.MAX_VALUE;
        int node = 0;
        for (int vertex = 1; vertex <= numberofvertices; vertex++)
        {
            if (unsettled[vertex] && key[vertex] < min)
            {
                node = vertex;
                min = key[vertex];
            }
        }
        return node;
    }

    public void evaluateNeighbours(int evaluationVertex)
    {

        for (int destinationvertex = 1; destinationvertex <= numberofvertices; destinationvertex++)
        {
            if (!settled[destinationvertex])
            {
                if (adjacencyMatrix[evaluationVertex][destinationvertex] != INFINITE)
                {
                    if (adjacencyMatrix[evaluationVertex][destinationvertex] < key[destinationvertex])
                    {
                        key[destinationvertex] = adjacencyMatrix[evaluationVertex][destinationvertex];
                        parent[destinationvertex] = evaluationVertex;
                    }
                    unsettled[destinationvertex] = true;
                }
            }
        }
    }

    public int[] getParent() {
        return parent;
    }


}