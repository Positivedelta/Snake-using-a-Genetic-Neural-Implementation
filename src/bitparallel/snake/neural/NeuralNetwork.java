//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.neural;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import bitparallel.snake.PsuedoRandom;
import bitparallel.snake.neural.activation.Activation;

public class NeuralNetwork
{
    private static final Logger logger = LogManager.getLogger(NeuralNetwork.class);

    final int numberOfInputs;
    private Neuron[][] layers;
    private int dimension;
    private NetworkState networkState;

    // this class assumes that the network is fully connected,
    //   1. the 1st layer will have hiddenLayersSize[0] neurons, each with numberOfInputs inputs
    //   2. the next inner hidden layers will have hiddenLayerSize[...] neurons, but each will have as many inputs as there were outputs in the previous layer
    //   3. the final layer has numberOfOutputs neurons, each with as many inputs as there are neurons in the preceeding layer
    //
    // note, each neuron has a hidden "always 1.0" input with a a specify [-1.0, 1.0] bias weight
    //
    public NeuralNetwork(final Activation activation, final int numberOfInputs, final int numberOfOutputs, final int ... hiddenLayerSize)
    {
        this.numberOfInputs = numberOfInputs;

        int neuronInputsForLayer = numberOfInputs;
        layers = new Neuron[1 + hiddenLayerSize.length][];
        logger.debug("Creating the neural network with " + numberOfInputs + " inputs, " + (hiddenLayerSize.length + 1) + " layers and " + numberOfOutputs + " outputs");
        logger.debug("Neuron activation: " + activation.getDescription());

        dimension = 0;
        for (int i = 0; i < hiddenLayerSize.length; i++)
        {
            final int layerSize = hiddenLayerSize[i];
            logger.debug("Layer #" + i + ", neurons: " + layerSize);

            layers[i] = new Neuron[layerSize];
            for (int n = 0; n < layerSize; n++) layers[i][n] = new Neuron(neuronInputsForLayer, activation);

            // account for the bias weight
            //
            dimension += (layerSize * (neuronInputsForLayer + 1));
            neuronInputsForLayer = layerSize;
        }

        layers[layers.length - 1] = new Neuron[numberOfOutputs];
        for (int n = 0; n < numberOfOutputs; n++) layers[layers.length - 1][n] = new Neuron(neuronInputsForLayer, activation);
        logger.debug("Layer #" + (layers.length - 1) + ", neurons: " + numberOfOutputs);

        // account for the bias weight
        //
        dimension += (numberOfOutputs * (neuronInputsForLayer + 1));
        logger.debug("Network dimension: " + dimension);
    }

    public final void setRandomState()
    {
        final double[][][] weights = new double[layers.length][][];
        final double[][] bias = new double[layers.length][];

        final PsuedoRandom random = PsuedoRandom.INSTANCE;
        int layerInputCount = numberOfInputs;
        for (int layer = 0; layer < layers.length; layer++)
        {
            weights[layer] = new double[layers[layer].length][];
            bias[layer] = new double[layers[layer].length];
            for (int neuron = 0; neuron < layers[layer].length; neuron++)
            {
                final double[] neuronWeights = new double[layerInputCount];
                for (int i = 0; i < layerInputCount; i++) neuronWeights[i] = (2.0 * random.nextDouble()) - 1.0;

                final double neuronBias = (2.0 * random.nextDouble()) - 1.0;
                layers[layer][neuron].setWeights(neuronWeights, neuronBias);

                weights[layer][neuron] = neuronWeights;
                bias[layer][neuron] = neuronBias;
            }

            // in a fully connected network the number of input for a given neuron in a given layer is equal to the number of outputs of the previous layer
            //
            layerInputCount = layers[layer].length;
        }

        networkState = new NetworkState(weights, bias, dimension);
    }

    public final void setState(final NetworkState networkState)
    {
        if (dimension != networkState.getDimension())
        {
            final StringBuffer errorMessage = new StringBuffer();
            errorMessage.append("The required dimension does not match the value provided in the NetworkState parameter, supplied: ");
            errorMessage.append(networkState.getDimension());
            errorMessage.append(", required: ");
            errorMessage.append(dimension);

            throw new IllegalArgumentException(errorMessage.toString());
        }

        this.networkState = networkState;

        final double[][][] weights = networkState.getWeights();
        final double[][] bias = networkState.getBias();

        // note, the number of allowed weights per neuron is not checked here, it's implemented by neuron.setWeights()
        //
        if (weights.length != layers.length) throw new IllegalArgumentException("Incorrect layer count inferred from the NetworkState weights[] array");
        for (int layer = 0; layer < layers.length; layer++)
        {
            final int neuronsInLayer = layers[layer].length;
            if (weights[layer].length != neuronsInLayer) throw new IllegalArgumentException("Incorrect neurons count inferred from the NetworkState weights[" + layer + "][] array");

            for (int neuron = 0; neuron < neuronsInLayer; neuron++)
            {
                final double[] neuronWeights = weights[layer][neuron];
                layers[layer][neuron].setWeights(neuronWeights, bias[layer][neuron]);
            }
        }
    }

    // note, head off any unexpected null reference exceptions
    //
    public final NetworkState getState()
    {
        if (networkState == null) throw new NullPointerException("No network state has been set");

        return networkState;
    }

    // iterate the neural network
    //
    public final double[] think(final double[] inputs)
    {
        if (inputs.length > numberOfInputs) throw new IllegalArgumentException("Too many network inputs, RXed: " + inputs.length + ", expected: " + numberOfInputs);

        double[] layerInputs = inputs;
        for (int layer = 0; layer < layers.length; layer++)
        {
            final int numberOfLayerNeurons = layers[layer].length;
            final double[] layerOutputs = new double[numberOfLayerNeurons];
            for (int neuron = 0; neuron < numberOfLayerNeurons; neuron++) layerOutputs[neuron] = layers[layer][neuron].think(layerInputs);
            layerInputs = layerOutputs;
        }

        // i.e. the network output, see last statement in above loop
        //
        return layerInputs;
    }
}
