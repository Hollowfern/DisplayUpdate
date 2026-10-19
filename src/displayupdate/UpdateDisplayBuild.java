package displayupdate;

import arc.struct.*;
import mindustry.world.blocks.logic.*;

/** Vanilla display building with a much larger command queue. */
public class UpdateDisplayBuild extends LogicDisplay.LogicDisplayBuild{
    public static final int maxQueue = 32768;

    public UpdateDisplayBuild(LogicDisplay outer){
        outer.super();
    }

    @Override
    public void draw(LongSeq graphicsBuffer){
        int added = Math.min(graphicsBuffer.size, maxQueue - commands.size);

        for(int i = 0; i < added; i++){
            commands.addLast(graphicsBuffer.items[i]);
        }

        operations++;
    }
}
