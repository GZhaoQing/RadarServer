package com.radar.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.MalformedURLException;

@Path("/r")
public class MyResource {

    @GET
    @Path("/{fileName}")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String represent(@PathParam("fileName")String file){
        return "..\\resources\\radarImage.jpg";
//        File f=new File(System.getProperty("user.dir")+"\\src\\main\\resources\\radarImg.jpg");

//        try {
//            return f.toURI().toURL().toString();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        return "nothing error!";
    }
}
