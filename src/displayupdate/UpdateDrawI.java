package displayupdate;

import arc.util.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.ui.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.logic.LogicDisplay.*;

/** Same as vanilla DrawI, but the processor-side buffer limit is much higher. */
public class UpdateDrawI extends LExecutor.DrawI{
    public static final int maxBuffer = 16384;

    public UpdateDrawI(LExecutor.DrawI o){
        super(o.type, o.x, o.y, o.p1, o.p2, o.p3, o.p4);
    }

    @Override
    public void run(LExecutor exec){
        if(Vars.headless || exec.graphicsBuffer.size >= maxBuffer) return;

        if(type == LogicDisplay.commandColorPack){
            double packed = x.num();

            int value = (int)(Double.doubleToRawLongBits(packed)),
            r = ((value & 0xff000000) >>> 24),
            g = ((value & 0x00ff0000) >>> 16),
            b = ((value & 0x0000ff00) >>> 8),
            a = ((value & 0x000000ff));

            exec.graphicsBuffer.add(DisplayCmd.get(LogicDisplay.commandColor, pk(r), pk(g), pk(b), pk(a), 0, 0));
        }else if(type == LogicDisplay.commandPrint){
            CharSequence str = exec.textBuffer;

            if(str.length() > 0){
                var data = Fonts.logic.getData();
                int advance = (int)data.spaceXadvance, lineHeight = (int)data.lineHeight;

                int xOffset, yOffset;
                int align = p1.numi();

                int maxWidth = 0, lines = 1, lineWidth = 0;
                for(int i = 0; i < str.length(); i++){
                    char next = str.charAt(i);
                    if(next == '\n'){
                        maxWidth = Math.max(maxWidth, lineWidth);
                        lineWidth = 0;
                        lines ++;
                    }else{
                        lineWidth ++;
                    }
                }
                maxWidth = Math.max(maxWidth, lineWidth);

                float
                width = maxWidth * advance,
                height = lines * lineHeight,
                ha = ((Align.isLeft(align) ? -1f : 0f) + 1f + (Align.isRight(align) ? 1f : 0f))/2f,
                va = ((Align.isBottom(align) ? -1f : 0f) + 1f + (Align.isTop(align) ? 1f : 0f))/2f;

                xOffset = -(int)(width * ha);
                yOffset = -(int)(height * va) + (lines - 1) * lineHeight;

                int curX = x.numi(), curY = y.numi();
                for(int i = 0; i < str.length(); i++){
                    char next = str.charAt(i);
                    if(next == '\n'){
                        curY -= lineHeight;
                        curX = x.numi();
                        continue;
                    }
                    if(Fonts.logic.getData().hasGlyph(next)){
                        exec.graphicsBuffer.add(DisplayCmd.get(LogicDisplay.commandPrint, pks(curX + xOffset), pks(curY + yOffset), next, 0, 0, 0));
                    }
                    curX += advance;

                    if(exec.graphicsBuffer.size >= maxBuffer) break;
                }

                exec.textBuffer.setLength(0);
            }
        }else{
            int num1 = pks(p1.numi()), num4 = pks(p4.numi()), xval = pks(x.numi()), yval = pks(y.numi());

            if(type == LogicDisplay.commandImage){
                int packed = -1;
                if(p1.obj() instanceof UnlockableContent){
                    UnlockableContent u = (UnlockableContent)p1.obj();
                    packed = (u.id << 5) | (u.getContentType().ordinal() & 31);
                }else if(p1.obj() instanceof LogicDisplayBuild){
                    LogicDisplayBuild d = (LogicDisplayBuild)p1.obj();
                    packed = (d.rootDisplay.index << 5) | LogicDisplay.displayDrawType;
                }
                num1 = packed & 0x3FF;
                num4 = packed >> 10;
            }else if(type == LogicDisplay.commandScale){
                xval = pks((int)(x.numf() / LogicDisplay.scaleStep));
                yval = pks((int)(y.numf() / LogicDisplay.scaleStep));
            }

            exec.graphicsBuffer.add(DisplayCmd.get(type, xval, yval, num1, pks(p2.numi()), pks(p3.numi()), num4));
        }
    }

    static int pk(int value){
        return value & 0b0111111111;
    }

    static int pks(int value){
        return (Math.abs(value) & 0b0111111111) | (value < 0 ? 0b1000000000 : 0);
    }
}
