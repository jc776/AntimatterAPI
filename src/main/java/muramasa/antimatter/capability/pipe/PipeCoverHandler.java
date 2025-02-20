package muramasa.antimatter.capability.pipe;

import muramasa.antimatter.capability.CoverHandler;
import muramasa.antimatter.tile.pipe.TileEntityPipe;

public class PipeCoverHandler<T extends TileEntityPipe> extends CoverHandler<T> {

    public PipeCoverHandler(T tile) {
        super(tile, tile.getValidCovers());
        // if (tag != null) deserialize(tag);
    }
}
