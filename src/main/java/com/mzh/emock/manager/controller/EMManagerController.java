package com.mzh.emock.manager.controller;

import com.google.gson.GsonBuilder;
import com.mzh.emock.core.context.EMContext;
import com.mzh.emock.manager.po.EMGroupExchange;
import com.mzh.emock.manager.service.EMManagerService;
import com.mzh.emock.manager.tool.ResourceTool;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mzh/em/manager")
public class EMManagerController {

    private static final String pageResPrefix="/static/page";
    private static final String indexPage="/index.html";
    private static final String indexPageNew="/index-new.html";

    private final Map<String,String> resourceCache=new HashMap<>();
    private final GsonBuilder builder=new GsonBuilder().serializeNulls();

    private final EMManagerService service;

    public EMManagerController(EMContext context){
        this.service=new EMManagerService(context);
    }


    @RequestMapping(indexPage)
    public String index(){
        if(resourceCache.get(indexPage)==null){
            resourceCache.put(indexPage, ResourceTool.loadResourceAsString(pageResPrefix+indexPage));
        }
        return resourceCache.get(indexPage);
    }

    @RequestMapping(indexPageNew)
    public String indexNew(){
        if(resourceCache.get(indexPageNew)==null){
            resourceCache.put(indexPageNew,ResourceTool.loadResourceAsString(pageResPrefix+indexPageNew));
        }
        return resourceCache.get(indexPageNew);
    }



    @RequestMapping(value = "/query",method = RequestMethod.POST)
    public String query(String oldObjectName,boolean includeObject,boolean includeProxy){
        return builder.create().toJson(service.query(oldObjectName,includeObject,includeProxy));
    }
    @RequestMapping(value = "/update",method = RequestMethod.POST)
    public String update(@RequestBody List<EMGroupExchange> exchange){
        return service.update(exchange);
    }
}
