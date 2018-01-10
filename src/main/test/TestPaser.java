import com.fasterxml.jackson.databind.ObjectMapper;
import com.radar.FileParser;
import com.radar.RadarFile;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class TestPaser {
    @Test
    public void TestDimension() throws IOException {
//        FileParser p=new FileParser(System.getProperty("user.dir")+"\\src\\main\\resources\\KFWD_SDUS64_NCZGRK_201208150217");
//        RadarFile rf=p.parseWithImg("defualtpath");
//        LinkedHashMap lm=(LinkedHashMap)rf.getHeadfile().getDimention();
//        Set ks=lm.keySet();
//        Iterator kit=ks.iterator();
//        while(kit.hasNext()){
//            System.out.println(kit.next());
//        }
    }
    @Test
    public void  TestAttribute() throws IOException {
//        FileParser p=new FileParser(System.getProperty("user.dir")+"\\src\\main\\resources\\KFWD_SDUS64_NCZGRK_201208150217");
//        RadarFile rf=p.parseWithImg("defualtpath");
//        Map lm=rf.getHeadfile().getAttribute();
//        Set ks=lm.keySet();
//        Iterator ait=ks.iterator();
//        while(ait.hasNext()){
//            System.out.println(lm.get(ait.next()));
//        }
    }
    @Test
    public void  TestVariable() throws IOException {
//        FileParser p=new FileParser(System.getProperty("user.dir")+"\\src\\main\\resources\\KFWD_SDUS64_NCZGRK_201208150217");
//        RadarFile rf=p.parseWithImg("defualtpath");
//        Map lm=rf.getHeadfile().getVariable();
//        Set ks=lm.keySet();
//        Iterator ait=ks.iterator();
//        while(ait.hasNext()){
//            System.out.println(ait.next());
//        }
    }
    @Test
    public void  Test2Json() throws IOException {
        FileParser p=new FileParser();
        RadarFile rf=p.readWithImg(System.getProperty("user.dir")+"\\src\\main\\resources\\KFWD_SDUS64_NCZGRK_201208150217","");
        ObjectMapper mapper=new ObjectMapper();
        String j=mapper.writeValueAsString(rf);
        System.out.println(j);
    }
}
