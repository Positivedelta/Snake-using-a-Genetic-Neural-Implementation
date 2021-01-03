//
// (c) Bit Parallel Ltd, January 2021
//

package bitparallel.snake.geometry;

public abstract class Movement
{
    protected int dx, dy;

    // notes 1, this method delagates Point transforamtions to the specific Point instance passed to this method
    //       2, the Point instance contains absolute an abosolute (x, y) coordinate
    //       3, the calculated dx and dy values represent directions, these could be absolute or relative
    //          the specific Point instance will know how to interpret and apply them to the returned Point instance
    //
    public Point translate(final Point point)
    {
        return point.translate(dx, dy);
    }
}
