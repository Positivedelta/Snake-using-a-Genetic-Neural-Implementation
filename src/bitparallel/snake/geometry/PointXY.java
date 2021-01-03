//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.geometry;

public class PointXY extends Point
{
    public PointXY(final int x, final int y)
    {
        super(x, y);
    }

    public final Point translate(final int dx, final int dy)
    {
        // notes 1, +dx == RIGHT, -dx == LEFT, +dy == DOWN, -dy == UP, i.e. absolute grid directions
        //       2, returns a new absolute PointXY so this method simply adds these absolute direction deltas
        //
        return new PointXY(x + dx, y + dy);
    }

    // required by List.contains() when checking for snake and food collisions
    //
    @Override
    public final boolean equals(final Object object)
    {
        return equals((PointXY)object);
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append("PointXY: [");
        sb.append(x);
        sb.append(", ");
        sb.append(y);
        sb.append("]");

        return sb.toString();
    }
}
