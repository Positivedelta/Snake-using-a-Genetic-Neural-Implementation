//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.neural.activation;

public class ReLU implements Activation
{
    public double threshold(final double input)
    {
        return Math.max(0.0, input);
    }

    public String getDescription()
    {
        return "ReLU";
    }
}
