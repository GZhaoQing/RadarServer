import com.radar.FileParser;
import com.radar.RadarGrid.RasterGrid2_Byte;
import org.junit.Test;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

import java.io.IOException;
import java.util.Formatter;
import java.util.Iterator;

public class TestFileRead {
    @Test
    public void testFile(){
        FileParser fp=new FileParser();
        RasterGrid2_Byte rg =fp.readFile(System.getProperty("user.dir")+"\\src\\main\\resources\\"+"SATE_L2_F2G_VISSR_MWB_LBT_SEC_LCN-IR2-20170527-0100.AWX");
        System.out.println(rg.m_llx+"\n"+rg.m_lly+"\n"+rg.m_cellSize+"\n"+rg.m_nRows+"\n" +rg.m_nCols+"\n"+rg.m_data);
    }
    @Test
    public void testRadial() throws IOException {
        String path=System.getProperty("user.dir")+"\\src\\main\\resources\\"+"KEWX_SDUS54_N0VEWX_201707271135";
        FileParser p=new FileParser();
        p.setImagePath(System.getProperty("user.dir")+"\\src\\main\\resources\\");
        RadialDatasetSweep rds=(RadialDatasetSweep) FeatureDatasetFactoryManager.open(
                FeatureType.RADIAL,
                path,
                null,
                new Formatter()
        );
        Iterator it = rds.getDataVariables().iterator();

        RadialDatasetSweep.RadialVariable var=(RadialDatasetSweep.RadialVariable)it.next();

        float[] data=var.readAllData();

    }
}
