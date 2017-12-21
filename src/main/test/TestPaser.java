import com.radar.RadarFileParser;
import com.radar.RadarHeadfile;
import org.junit.Test;

import java.util.*;

public class TestPaser {
    @Test
    public void TestDimension(){
        RadarFileParser p=new RadarFileParser();
        RadarHeadfile hf=p.parse("defualtpath");
        LinkedHashMap lm=(LinkedHashMap) hf.getDimention();
        Set ks=lm.keySet();
        Iterator kit=ks.iterator();
        while(kit.hasNext()){
            System.out.println(kit.next());
        }
    }
    @Test
    public void  TestAttribute(){
        RadarFileParser p=new RadarFileParser();
        RadarHeadfile hf=p.parse("defualtpath");
        Map lm=hf.getAttribute();
        Set ks=lm.keySet();
        Iterator ait=ks.iterator();
        while(ait.hasNext()){
            System.out.println(lm.get(ait.next()));
        }
    }
}
