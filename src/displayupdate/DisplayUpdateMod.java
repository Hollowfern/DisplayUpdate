package displayupdate;

import arc.func.*;
import arc.util.*;
import mindustry.*;
import mindustry.mod.*;
import mindustry.logic.*;
import mindustry.logic.LStatements.*;
import mindustry.world.*;
import mindustry.world.blocks.logic.*;

public class DisplayUpdateMod extends Mod{

    /** Draw statement that builds the raised-limit instruction. Looks and saves exactly like vanilla "draw". */
    public static class UpdateDrawStatement extends DrawStatement{
        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            LExecutor.LInstruction i = super.build(builder);
            if(i instanceof LExecutor.DrawI && !(i instanceof UpdateDrawI)){
                return new UpdateDrawI((LExecutor.DrawI)i);
            }
            return i;
        }

        @Override
        public String name(){
            return "Draw";
        }

        @Override
        public void write(StringBuilder builder){
            DrawStatement plain = new DrawStatement();
            plain.type = type;
            plain.x = x;
            plain.y = y;
            plain.p1 = p1;
            plain.p2 = p2;
            plain.p3 = p3;
            plain.p4 = p4;
            plain.write(builder);
        }
    }

    @Override
    public void init(){
        //1. processor side: replace the parser for "draw"
        final Func<String[], LStatement> orig = LAssembler.customParsers.get("draw");
        if(orig == null){
            Log.warn("[display-update] no parser for 'draw' found, processor limit NOT raised.");
        }else{
            LAssembler.customParsers.put("draw", arr -> {
                LStatement s = orig.get(arr);
                if(s instanceof DrawStatement && !(s instanceof UpdateDrawStatement)){
                    DrawStatement d = (DrawStatement)s;
                    UpdateDrawStatement b = new UpdateDrawStatement();
                    b.type = d.type;
                    b.x = d.x;
                    b.y = d.y;
                    b.p1 = d.p1;
                    b.p2 = d.p2;
                    b.p3 = d.p3;
                    b.p4 = d.p4;
                    return b;
                }
                return s;
            });
        }

        //2. display side: every vanilla display now creates the big-queue building
        for(Block b : Vars.content.blocks()){
            if(b instanceof LogicDisplay){
                final LogicDisplay d = (LogicDisplay)b;
                d.buildType = () -> new UpdateDisplayBuild(d);
            }
        }
    }
}
