import com.fasterxml.jackson.databind.ObjectMapper;
import com.radar.RadarFile;
import com.radar.RadarFileParser;
import com.radar.RadarHeadfile;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class TestPaser {
    @Test
    public void TestDimension() throws IOException {
        RadarFileParser p=new RadarFileParser();
        RadarFile rf=p.parse("defualtpath");
        LinkedHashMap lm=(LinkedHashMap)rf.getHeadfile().getDimention();
        Set ks=lm.keySet();
        Iterator kit=ks.iterator();
        while(kit.hasNext()){
            System.out.println(kit.next());
        }
    }
    @Test
    public void  TestAttribute() throws IOException {
        RadarFileParser p=new RadarFileParser();
        RadarFile rf=p.parse("defualtpath");
        Map lm=rf.getHeadfile().getAttribute();
        Set ks=lm.keySet();
        Iterator ait=ks.iterator();
        while(ait.hasNext()){
            System.out.println(lm.get(ait.next()));
        }
    }
    @Test
    public void  TestVariable() throws IOException {
        RadarFileParser p=new RadarFileParser();
        RadarFile rf=p.parse("defualtpath");
        Map lm=rf.getHeadfile().getVariable();
        Set ks=lm.keySet();
        Iterator ait=ks.iterator();
        while(ait.hasNext()){
            System.out.println(ait.next());
        }
    }
    @Test
    public void  Test2Json() throws IOException {
        RadarFileParser p=new RadarFileParser();
        RadarFile rf=p.parse("defualtpath");
        ObjectMapper mapper=new ObjectMapper();
        String j=mapper.writeValueAsString(rf);
        System.out.println(j);
    }
}
