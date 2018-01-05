package com.radar;

import ucar.ma2.DataType;
import ucar.nc2.*;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
/*将文件中的头文件信息和数据图像封装到对象*/
public class RadarFileParser {
    private String defaultPath=Thread.currentThread().getContextClassLoader().getResource("" ).toString();
    private String fileIndefault = "KFWD_SDUS64_NCZGRK_201208150217";
    private String imagePath;



    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
