//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.neural;

import bitparallel.snake.neural.activation.Activation;

//
// note, each neuron has a hidden bias input that is always set to 1.0 and this requires a specific bias weight
//

public class Neuron
{
    private final int numberOfInputs;
    private final Activation activation;
    private double[] weights;
    private double bias;

    public Neuron(final int numberOfInputs, final Activation activation)
    {
        this.numberOfInputs = numberOfInputs;
        this.activation = activation;
    }

    public final Activation getActivation()
    {
        return activation;
    }

    public final void setWeights(final double[] weights, final double bias)
    {
        if (weights.length != numberOfInputs) throw new IllegalArgumentException("Incorrect number of weights: " + weights.length + ", requires: " + numberOfInputs);
        this.weights = weights;
        this.bias = bias;
    }

    public final void setWeights(final double[] weights)
    {
        if (weights.length != numberOfInputs) throw new IllegalArgumentException("Incorrect number of weights: " + weights.length + ", requires: " + numberOfInputs);
        this.weights = weights;
    }

    // note, head off any unexpected null reference exceptions
    //
    public final double[] getWeights()
    {
        if (weights == null) throw new NullPointerException("No weights have been set for this neuron");

        return weights;
    }

    public final void setBias(final double bias)
    {
        this.bias = bias;
    }

    public final double getBias()
    {
        return bias;
    }

    public final double think(final double[] inputs)
    {
        double weightedSum = 0.0;
        for (int i = 0; i < inputs.length; i++) weightedSum += (inputs[i] * weights[i]);

        // add bias, assumes a hidden input of 1.0
        //
        weightedSum += bias;

        return activation.threshold(weightedSum);
    }
}
