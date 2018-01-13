import com.radar.FileParser;
import com.radar.radargrid.RasterGrid2_Byte;
import org.junit.Test;
import ucar.nc2.NCdumpW;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Formatter;
import java.util.Iterator;

public class TestFileRead {
    @Test
    public void DumpTest() throws IOException {
        String fileIn=System.getProperty("user.dir")+"\\src\\main\\resources\\spherical_pressure_level.grib1";
        NCdumpW.print(fileIn, new PrintWriter(System.out));
    }
    @Test
    public void testFile() throws IOException {
        FileParser fp=new FileParser();
        RasterGrid2_Byte rg =fp.readGridData(System.getProperty("user.dir")+"\\src\\main\\resources\\"+"SATE_L2_F2G_VISSR_MWB_LBT_SEC_LCN-IR2-20170527-0100.AWX");
        System.out.println(rg.m_llx+"\n"+rg.m_lly+"\n"+rg.m_cellSize+"\n"+rg.m_nRows+"\n" +rg.m_nCols+"\n"+rg.m_data);
    }
    @Test
    public void testRadial() throws IOException {
        long ts=System.currentTimeMillis();

        String path=System.getProperty("user.dir")+"\\src\\main\\resources\\"+"KEWX_SDUS54_N0VEWX_201707270600";

        RadialDatasetSweep rds=(RadialDatasetSweep) FeatureDatasetFactoryManager.open(
                FeatureType.RADIAL,
                path,
                null,
                new Formatter()
        );
        Iterator it = rds.getDataVariables().iterator();

        RadialDatasetSweep.RadialVariable var=(RadialDatasetSweep.RadialVariable)it.next();
        float[] data=var.readAllData();
        RadialDatasetSweep.Sweep sweep=var.getSweep(0);
//        System.out.println(sweep.getBeamWidth());
        System.out.println(sweep.getRangeToFirstGate());
        float[] azimuth=sweep.getAzimuth();
        int aziLength=azimuth.length;
        float gSize=sweep.getGateSize();
        System.out.println(gSize);
        int gNum=sweep.getGateNumber();
        System.out.println(gNum);

        BufferedImage bi=new BufferedImage(gNum*2,gNum*2,BufferedImage.TYPE_3BYTE_BGR);
        int v=0;float rv;
        int offX=0;
        int offY=0;
        for(int i=0;i<aziLength;i++){
//            oneG=sweep.readData(i);
//            System.out.println(azimuth[i]);
            for(int j=0;j<gNum;j++){
                rv=data[i*230+j];

//                rv=oneG[j];
                if(rv==rv){
                    v=(int)rv;
                }else{
                    v=-1000;
                }
//                System.out.println(v);
                offX= (int) (gNum+Math.cos((azimuth[i]-90)/180*Math.PI)*j);
                offY= (int) (gNum+Math.sin((azimuth[i]-90)/180*Math.PI)*j);
                //绘制
                switch (v){
                    case 64:bi.setRGB(offX,offY,0xFF0000);break;
                    case 50:bi.setRGB(offX,offY,0xD07A00);break;
                    case 36:bi.setRGB(offX,offY,0xAE0000);break;
                    case 26:bi.setRGB(offX,offY,0xFFFF00);break;
                    case 20:bi.setRGB(offX,offY,0xFFCF00);break;
                    case 10:bi.setRGB(offX,offY,0xF88700);break;
                    case 0:bi.setRGB(offX,offY,0x767676);
                         break;
                    case -1:bi.setRGB(offX,offY,0xCDC09F);break;
                    case -10:bi.setRGB(offX,offY,0x008F00);break;
                    case -20:bi.setRGB(offX,offY,0x00BB00);break;
                    case -26:bi.setRGB(offX,offY,0x00FB90);break;
                    case -36:bi.setRGB(offX,offY,0x320096);break;
                    case -50:bi.setRGB(offX,offY,0x008AFF);break;
                    case -64:bi.setRGB(offX,offY,0x00E0FF);break;
                    default://bi.setRGB(offX,offY,0x77007D);
                         break;
                }


            }
        }
        File file=new File(path+"img.jpg");
        ImageIO.write(bi,"jpg",file);


        long se=System.currentTimeMillis();
        System.out.println(se-ts);

        // 实验
//        float[] oneG=sweep.readData(0);
//        int k=0;
//        for(float f:oneG){
//            System.out.println(f+"::"+data[k++]);
//        }
    }
}
