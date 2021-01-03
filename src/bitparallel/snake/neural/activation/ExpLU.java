//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.neural.activation;

public class ExpLU implements Activation
{
    private final double alpha;

    public ExpLU()
    {
        alpha = 0.3;
    }

    public ExpLU(final double alpha)
    {
        this.alpha = alpha;
    }

    public double threshold(final double input)
    {
        if (input >= 0.0) return input;

        return alpha * (Math.exp(input) -1);
    }

    public String getDescription()
    {
        return "ExpLU";
    }
}
