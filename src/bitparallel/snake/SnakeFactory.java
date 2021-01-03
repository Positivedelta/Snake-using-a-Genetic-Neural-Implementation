//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import bitparallel.snake.neural.NeuralNetwork;
import bitparallel.snake.neural.activation.Activation;
import bitparallel.snake.neural.activation.ReLU;

public class SnakeFactory
{
    private static final Logger logger = LogManager.getLogger(SnakeFactory.class);

    private final SnakeSpecies species;
    private final int gridWidth, gridHeight;

    public SnakeFactory(final SnakeSpecies species, final int gridWidth, final int gridHeight)
    {
        this.species = species;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        switch (species)
        {
            case FullMovement:
                logger.info("Configured to generate FullMovementSnake instances");
                break;

            case ForwardOnly:
                logger.info("Configured to generate ForwardOnlySnake instances");
                break;

            default:
                throw new IllegalArgumentException("Unable to build the Snake instance, bad SnakeSpecies enum");
        }
    }

    public final int getGridWidth()
    {
        return gridWidth;
    }

    public final int getGridHeight()
    {
        return gridHeight;
    }

    public final Snake getSnakeInstance()
    {
        final Snake snake;
        switch (species)
        {
            // a snake that can move up, down, left and right
            //
            case FullMovement:
            {
                final int nnInputs = 24;
                final int nnSizeHiddenLayer1 = 16;
                final int nnSizeHiddenLayer2 = 20;
                final int nnOutputs = 4;
                final Activation activation = new ReLU();
                final NeuralNetwork brain = new NeuralNetwork(activation, nnInputs, nnOutputs, nnSizeHiddenLayer1, nnSizeHiddenLayer2);

                snake = new FullMovementSnake(brain, gridWidth, gridHeight);
                break;
            }

            // a simple NWSE looking snake that can only move forwards, left and right
            //
            case ForwardOnly:
            {
                final int nnInputs = 6;
                final int nnSizeHiddenLayer1 = 6;
                final int nnSizeHiddenLayer2 = 8;
                final int nnOutputs = 3;
                final Activation activation = new ReLU();
                final NeuralNetwork brain = new NeuralNetwork(activation, nnInputs, nnOutputs, nnSizeHiddenLayer1, nnSizeHiddenLayer2);

                snake = new ForwardOnlySnake(brain, gridWidth, gridHeight);
                break;
            }

            default:
                throw new IllegalArgumentException("Unable to build the Snake instance, bad SnakeSpecies enum");
        }

        return snake;
    }
}
