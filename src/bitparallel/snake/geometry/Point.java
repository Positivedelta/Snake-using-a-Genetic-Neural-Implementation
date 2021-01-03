//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.geometry;

public abstract class Point
{
    protected final int x, y;

    protected Point(final int x, final int y)
    {
        this.x = x;
        this.y = y;
    }

    public abstract Point translate(final int dx, final int dy);

    public final int getX()
    {
        return x;
    }

    public final int getY()
    {
        return y;
    }

    // required by contains() in List
    //
    public final boolean equals(final Point point)
    {
        return (x == point.x) && (y == point.y);
    }
}
