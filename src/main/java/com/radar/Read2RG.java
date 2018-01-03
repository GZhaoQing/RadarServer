package com.radar;

/*
*设计思路：接收一个存储着数据源NetcdfDataset对象ncds
*1. 通过FeatureDatasetFactoryManager将ncds读取到FeatureDataset对象fds，对fds操作，获得存储的影像数据(Array)和对应的Dimensions列表;
*2. 回到ncds，通过Dimension名称从ncds获取到CoordinateAxis对象，判断其类型AxisType，筛选两个表示平面的维度（x，y）;
*3. 从CoordinateAxis可以获取坐标数据的最大值和最小值，结合shape信息表示的数据数量，可以计算出RasterGrid2_Byte对象中的栅格单元边长；最小值作为数据起始位置和;
*4. 通过判断出的x,y维度对应的Length，将数据从Array存储到二维数据。
*/
public class Read2RG {
}
