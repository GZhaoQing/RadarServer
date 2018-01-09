package com.radar;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/*
* 根据传入的数据绘图
* */
public class ImageCreator {
    public final static int GRAY=1;
    public final static int RGB_GRID=2;
    public final static int RGB_RADIAL=3;
    private String imagePath;

    public ImageCreator(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String createImage(int type,int[] data,int width,int height,String fileName) throws IOException {
        if(type==RGB_GRID){
            return writeImg(createRGBImg(data,width,height),fileName);
        }else if(type==GRAY){
            return writeImg(createPixImg(data,width,height),fileName);
        } else{
            return null;
        }
    }

    public String createImage(int type,float[] data,float[] azimuth,int gate,String fileName) throws IOException {
        if(type==RGB_RADIAL){
            return writeImg(createRadialImg(data,azimuth,gate),fileName);
        } else{
            return null;
        }
    }

    private String writeImg(BufferedImage bi,String fileName) throws IOException {
        String filePath;
        if(imagePath!=null){
            filePath=imagePath;
        }else{
//           filePath="file:/E:/win7sp1/apache-tomcat-8.0.38-windows-x64/apache-tomcat-8.0.38/webapps/radar/img/";
            filePath="";
        }
        String imgPath=fileName+".jpg";

        File file=new File(filePath+imgPath);
        if (!file.exists()) {
            file.createNewFile();
        }
        ImageIO.write(bi, "jpg", file);
        return imgPath;
    }


    private BufferedImage createRGBImg(int[] data,int width,int height) throws IOException {
        BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
        for(int i=0 ;i<height;i++){
            for(int j = 0 ;j<width;j++){
                //按列读取
                switch (data[j*width+i]){
                    case 0:bi.setRGB(i,j,0xFAFAFA);break;
                    case 1:bi.setRGB(i,j,0xBFBFFC);break;
                    case 2:bi.setRGB(i,j,0x7870ED);break;
                    case 3:bi.setRGB(i,j,0x1C70CF);break;
                    case 4:bi.setRGB(i,j,0xA6FAA6);break;
                    case 5:bi.setRGB(i,j,0x00E800);break;
                    case 6:bi.setRGB(i,j,0x0F911A);break;
                    case 7:bi.setRGB(i,j,0xFAF263);break;
                    case 8:bi.setRGB(i,j,0xC7C712);break;
                    case 9:bi.setRGB(i,j,0x898900);break;
                    case 10:bi.setRGB(i,j,0xFCABAB);break;
                    case 11:bi.setRGB(i,j,0xFC5C5C);break;
                    case 12:bi.setRGB(i,j,0xED122E);break;
                    case 13:bi.setRGB(i,j,0xD48AFF);break;
                    case 14:bi.setRGB(i,j,0xA524FA);break;
                    default:bi.setRGB(i,j,0x000000);break;
                }
            }
        }
        return bi;
    }


    private BufferedImage createPixImg(int[] data,int width,int height) throws IOException {
        BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
        bi.getRaster().setPixels(0,0,width,height,data);
        return  bi;

    }


    private BufferedImage createRadialImg(float[] data,float[] azimuth,int gateNum) throws IOException {
        BufferedImage bi = new BufferedImage(gateNum*2,gateNum*2,BufferedImage.TYPE_3BYTE_BGR);
        int v;
        float rv;
        int offX;
        int offY;
        for(int i=0;i<azimuth.length;i++){
//            System.out.println(azimuth[i]);
            for(int j=0;j<gateNum;j++){
                rv=data[i*230+j];
                if(rv==rv){
                    v=(int)rv;
                }else{
                    v=-1000;
                }
//                System.out.println(v);
                offX= (int) (gateNum+Math.cos((azimuth[i]-90)/180*Math.PI)*j);
                offY= (int) (gateNum+Math.sin((azimuth[i]-90)/180*Math.PI)*j);
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


        return bi;
    }
}
