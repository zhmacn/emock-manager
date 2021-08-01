package com.mzh.emock.manager.tool;

import com.mzh.emock.manager.controller.EMManagerController;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ResourceTool {
    private static final Charset resCharset= StandardCharsets.UTF_8;
    public static String loadResourceAsString(String pathName){
        try(InputStream is= EMManagerController.class.getResourceAsStream(pathName)){
            if(is==null){
                return null;
            }
            byte[] ba=new byte[is.available()];
            int c=is.read(ba,0,ba.length);
            return new String(ba,0,c,resCharset);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
