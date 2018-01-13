import org.junit.Test;
import thredds.featurecollection.FeatureCollectionConfig;
import thredds.inventory.CollectionUpdateType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.grib.collection.Grib1CollectionBuilder;
import ucar.nc2.grib.collection.Grib2PartitionBuilderFromIndex;
import ucar.nc2.grib.collection.GribCdmIndex;
import ucar.nc2.grib.grib1.*;
import ucar.nc2.grib.grib2.Grib2Record;
import ucar.nc2.grib.grib2.Grib2RecordScanner;
import ucar.nc2.grib.grib2.Grib2SectionData;
import ucar.nc2.grib.grib2.Grib2SectionDataRepresentation;
import ucar.unidata.io.RandomAccessFile;


import java.io.IOException;
import java.util.Formatter;


public class TestGrib {
    @Test
    public void TestGrib1() throws IOException {
        String location=System.getProperty("user.dir")+"\\src\\main\\resources\\spherical_pressure_level.grib1";//spherical_pressure_level,complex_packing
        RandomAccessFile raf=new RandomAccessFile(location,"r");
        Grib1RecordScanner scan = new Grib1RecordScanner(raf);

        while (scan.hasNext()) {
            ucar.nc2.grib.grib1.Grib1Record gr1 = scan.next();
            Formatter f=new Formatter();
            gr1.showDataInfo(raf,f);
            System.out.println(f);
            Grib1Gds gds=gr1.getGDS();
//            gr1.readData(raf);

            System.out.println(gr1.getGDS());
            System.out.println("Grib1 SBD:");
            Grib1SectionBinaryData sbd=gr1.getDataSection();

            byte[] d=sbd.getBytes(raf);
            for(byte v:d){
                System.out.print(v+";");
            }
            System.out.println(gr1.getDataSection()+"lenth:"+sbd.getLength());
            Grib1SectionGridDefinition sgd=gr1.getGDSsection();
            System.out.println(gr1.getGDSsection());

            System.out.println(gr1.getBitMapSection());
            // do good stuff

        }


//        System.out.println(gr1.readData(raf))
        raf.close();


    }
    @Test
    public void testGrib2() throws IOException {
        String location=System.getProperty("user.dir")+"\\src\\main\\resources\\spherical_pressure_level.grib2";
        RandomAccessFile raf=new RandomAccessFile(location,"r");
        Grib2RecordScanner scan = new Grib2RecordScanner(raf);
        while (scan.hasNext()) {
            Grib2Record gr2 = scan.next();
            Grib2SectionDataRepresentation sdr=gr2.getDataRepresentationSection();
            gr2.readData(raf,sdr.getStartingPosition());
            Grib2SectionData sd=gr2.getDataSection();
            byte[] d=sd.getBytes(raf);
            for(byte v:d){
                System.out.print(v+";");
            }
        }
    }

}
