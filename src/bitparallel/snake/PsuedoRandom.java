//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake;

import java.util.Random;

//
// to avoid bias, all snake classes will use this singleton to generate random numbers from a single source
//

public enum PsuedoRandom
{
    INSTANCE;

    private Random random;

    private PsuedoRandom()
    {
        random = new Random();
    }

    //
    // only proxy the required methods
    //

    public final int nextInt(final int exclusiveMaximum)
    {
        return random.nextInt(exclusiveMaximum);
    }

    public final double nextGaussian()
    {
        return random.nextGaussian();
    }

    public final double nextDouble()
    {
        return random.nextDouble();
    }

    // for completeness, but its usage syntax is a little verbose, i.e. use / add to the methods above
    //
    public final Random getGenerator()
    {
        return random;
    }
}
