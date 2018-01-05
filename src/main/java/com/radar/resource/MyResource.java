package com.radar.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radar.FileParser;
import com.radar.RadarFile;
import com.radar.RadarFileParser;


import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

@Path("/r")
public class MyResource {

    @GET
    @Path("/{fileName}")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String represent(@PathParam("fileName")String file , @Context ServletContext context) throws MalformedURLException {
        FileParser p=new FileParser();
        try {
            double ts=System.currentTimeMillis();
            p.setImagePath(context.getResource("img").getPath());
            RadarFile rf=p.parse(Thread.currentThread().getContextClassLoader().getResource("KFWD_SDUS64_NCZGRK_201208150217" ).toString(),"KFWD_SDUS64_NCZGRK_201208150217");
            double te = System.currentTimeMillis();
            System.out.println(te-ts+"ms");
            ObjectMapper mapper=new ObjectMapper();
            String j=mapper.writeValueAsString(rf);
            return j;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

//        return Thread.currentThread().getContextClassLoader().getResource("" ).toString()+"\n"+
//                context.getResource("img").getPath();

    }
}
