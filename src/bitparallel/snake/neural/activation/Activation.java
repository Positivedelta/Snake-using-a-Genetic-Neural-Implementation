//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.neural.activation;

public interface Activation
{
    public double threshold(final double input);
    public String getDescription();
}
