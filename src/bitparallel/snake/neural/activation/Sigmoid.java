//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.neural.activation;

public class Sigmoid implements Activation
{
    private final double slope;

    public Sigmoid()
    {
        slope = 1.0;
    }

    // note, to widen the sigmoid use slope values < 1.0
    //
    public Sigmoid(double slope)
    {
        this.slope = slope;
    }

    public final double threshold(final double input)
    {
        if (input > 10.0) return 0.0;
        if (input < -10.0) return 1.0;

        final double power = slope * input;
        return 1.0 / (1.0 + Math.exp(-power));
    }

    public String getDescription()
    {
        return "Sigmoid, slope = 1.0, limits[-10.0, 10.0]";
    }
}
