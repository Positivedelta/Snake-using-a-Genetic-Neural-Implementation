//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.neural;

public class NetworkState
{ 
    private final int dimension;
    private final double[][][] weights;
    private final double[][] bias;

    public NetworkState(final double[][][] weights, final double[][] bias, final int dimension)
    {
        this.weights = weights;
        this.bias = bias;
        this.dimension = dimension;
    }

    public final int getDimension()
    {
        return dimension;
    }

    public final double[][][] getWeights()
    {
        return weights;
    }

    public final double[][] getBias()
    {
        return bias;
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append("\n");

        for (int layer = 0; layer < weights.length; layer++)
        {
            sb.append("Layer #");
            sb.append(layer);
            sb.append(", ");
            sb.append(weights[layer].length);
            sb.append(" Neurons [");
            sb.append(weights[layer][0].length);
            sb.append("i, ");
            sb.append(weights[layer][0].length);
            sb.append("w, 1b]\n");
        }

        sb.append("\nTotal Weights and Biases: ");
        sb.append(dimension);
        sb.append("\n\n");

        return sb.toString();
    }
}
