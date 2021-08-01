package com.mzh.emock.manager.service;

import com.mzh.emock.core.compiler.EMMemoryClassLoader;
import com.mzh.emock.core.compiler.EMRTCompiler;
import com.mzh.emock.core.compiler.result.EMCompilerResult;
import com.mzh.emock.core.context.EMContext;
import com.mzh.emock.core.type.EMObjectGroup;
import com.mzh.emock.core.type.object.EMObjectInfo;
import com.mzh.emock.core.type.object.field.EMFieldInfo;
import com.mzh.emock.core.type.object.method.EMMethodInfo;
import com.mzh.emock.core.type.object.method.EMMethodInvoker;
import com.mzh.emock.core.type.object.method.EMMethodSignature;
import com.mzh.emock.core.type.proxy.EMProxyHolder;
import com.mzh.emock.core.util.EMObjectUtil;
import com.mzh.emock.core.util.EMStringUtil;
import com.mzh.emock.manager.po.EMInvokerCreateResult;
import com.mzh.emock.manager.po.EMGroupExchange;
import com.mzh.emock.manager.po.EMGroupExchange.*;
import com.mzh.emock.manager.template.EMCodeTemplate;
import com.mzh.emock.manager.tool.ResourceTool;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EMManagerService {
    private final EMContext context;

    public EMManagerService(EMContext context){
        this.context=context;
    }

    public Map<String,Object> query(String name,boolean includeObject,boolean includeProxy){
        if(this.context==null){
            return Collections.singletonMap("show","error ,context is null");
        }
        Map<String,Object> res=new HashMap<>();
        List<EMGroupExchange> gl=new ArrayList<>();
        List<EMMethodSignatureExchange> mss=new ArrayList<>();
        for(Object obj:context.getOldObjects()){
            EMObjectGroup<?> group=context.getObjectGroup(obj);
            if(EMStringUtil.isEmpty(name) || group.getObjectName().contains(name)){
                EMGroupExchange g=new EMGroupExchange();
                g.setId(group.getId());
                g.setOldObjectName(group.getObjectName());
                g.setOldObjectClass(group.getOldObject().getClass().getName());
                g.setOldObject(group.getOldObject().toString());
                g.setMockInfo(toRTExchange(group,includeObject,includeProxy));
                gl.add(g);
                mss.addAll(parseMethodSignature(group));
            }
        }
        res.put("show",gl);//show mock info in json format
        res.put("hide",mss); //hide value of method signature for dynamic method invoke
        return res;
    }

    private <T> List<EMMethodSignatureExchange> parseMethodSignature(EMObjectGroup<T> group){
        List<EMMethodSignatureExchange> msl=new ArrayList<>();
        List<Class<? super T>> cl=group.getMockClass();
        for(Class<? super T> c:cl){
            List<? extends EMObjectInfo<? super T,?>> el=group.getMockInfo(c);
            for(EMObjectInfo<? super T,?> e:el){
                Map<String,EMMethodInfo<?>> mm=e.getInvokeMethods();
                for(String mn:mm.keySet()){
                    EMMethodSignatureExchange ms=new EMMethodSignatureExchange();
                    EMMethodSignature msi=mm.get(mn).getSignature();
                    ms.setObjectId(e.getId());
                    ms.setMethodName(mm.get(mn).getName());
                    ms.setReturnType(msi.getReturnType());
                    ms.setSimpleSignature(msi.getSimpleSignature());
                    ms.setImportCode(msi.getImportList().stream().reduce("",(o,n)->o+"import "+n+";\r\n"));
                    ms.setParameterList(msi.getParameterList());
                    msl.add(ms);
                }
            }
        }
        return msl;
    }

    @SuppressWarnings("unchecked")
    public <T> String update(List<EMGroupExchange> exchanges){
        if(exchanges==null || context==null){
            return "update failed exchange is null";
        }
        StringBuilder sb=new StringBuilder();
        for(EMGroupExchange ge:exchanges){
            EMObjectGroup<T> og= (EMObjectGroup<T>) findRelationById(ge.getId());
            if(og!=null){
                for(String cn:ge.getMockInfo().getMockedObjects().keySet()){
                    for(Class<? super T> c:og.getMockClass()){
                        if(c.getName().equals(cn)){
                            List<? extends EMObjectInfo<?,?>> oil= og.getMockInfo(c);
                            List<EMObjectExchange> el=ge.getMockInfo().getMockedObjects().get(cn);
                            sb.append(updateList(el,oil));
                        }
                    }
                }
            }
        }
        return sb.toString().equals("")?"success":sb.toString();
    }
    private String updateList(List<EMObjectExchange> oel,List<? extends EMObjectInfo<?,?>> oil){
        StringBuilder sb=new StringBuilder(256);
        for(EMObjectExchange oe:oel){
            for(EMObjectInfo<?,?> oi:oil){
                if(oe.getId()==oi.getId()){
                    sb.append(updateSingle(oe,oi));
                }
            }
        }
        oil.sort(Comparator.comparingInt(e->e.getDefinition().getOrder()));
        return sb.toString();
    }

    private String updateSingle(EMObjectExchange oe, EMObjectInfo<?,?> oi){
        StringBuilder sb=new StringBuilder(256);
        oi.setMocked(oe.isMock());
        oi.getDefinition().setOrder(oe.getOrder());
        for(EMMethodExchange me:oe.getMethodInfo()){
            EMMethodInfo<?> mi=oi.getInvokeMethods().get(me.getMethodName());
            mi.setMock(me.isMock());
            String updateInfo=updateDynamicInvoker(mi,me);
            if(!EMStringUtil.isEmpty(updateInfo)){
                sb.append(updateInfo).append("\r\n");
            }
        }
        return sb.toString();
    }

    private <T> String updateDynamicInvoker(EMMethodInfo<T> mi, EMMethodExchange me){
        String resultStr="";
        for(String dmn:me.getDynamicInvokes().keySet()){
            EMDynamicInvokeExchange die=me.getDynamicInvokes().get(dmn);
            String sc=die.getSrcCode();
            if(!EMStringUtil.isEmpty(sc)){
                EMMethodInvoker<T> ci=mi.getDynamicInvokers().get(dmn);
                if(ci==null || !sc.equals(ci.getCode())) {
                    EMInvokerCreateResult<T> result=createInvoker(mi,sc);
                    if(result.isSuccess()){
                        mi.getDynamicInvokers().put(dmn, result.getInvoker());
                    }else{
                        resultStr="update failed,name:"+dmn+",msg:"+result.getMessage();
                    }
                }
            }
        }
        //remove
        List<String> removeKeys=new ArrayList<>();
        for(String key:mi.getDynamicInvokers().keySet()){
            boolean flag=false;
            for(String eKey:me.getDynamicInvokes().keySet()){
                if(key.equals(eKey)){
                    flag=true;
                    break;
                }
            }
            if(!flag){
                removeKeys.add(key);
            }
        }
        for(String rKey:removeKeys){
            mi.getDynamicInvokers().remove(rKey);
        }
        if(me.getDynamicInvokeName()!=null && mi.getDynamicInvokers().size()>0
                && mi.getDynamicInvokers().get(me.getDynamicInvokeName())!=null){
            mi.setEnabledDynamicInvoker(me.getDynamicInvokeName());
        }else{
            mi.setEnabledDynamicInvoker(null);
        }
        return resultStr;
    }

    @SuppressWarnings("unchecked")
    private <T> EMInvokerCreateResult<T> createInvoker(EMMethodInfo<T> methodInfo,String srcCode){
        String[] codes=srcCode.split("_");
        if(codes.length!=2 || EMStringUtil.isEmpty(codes[1])){
            return new EMInvokerCreateResult<>(false,"code format error",null);
        }
        String importPart= EMStringUtil.isEmpty(codes[0])?"":new String(Base64.getDecoder().decode(codes[0]), StandardCharsets.UTF_8);
        String codePart=new String(Base64.getDecoder().decode(codes[1]),StandardCharsets.UTF_8);
        String clzName="EMDynamicInvoker_i_"+ EMObjectUtil.getNextId();
        Map<String,String> codePlaceHolder=new HashMap<>();
        codePlaceHolder.put(EMCodeTemplate.NAME_HOLDER,clzName);
        codePlaceHolder.put(EMCodeTemplate.IMPORT_HOLDER,importPart);
        codePlaceHolder.put(EMCodeTemplate.CODE_HOLDER,codePart);
        codePlaceHolder.put(EMCodeTemplate.RETURN_HOLDER,methodInfo.getSignature().getReturnType());
        String fullCodeStr=generateCode(EMCodeTemplate.simpleInvokeTemplatePath,codePlaceHolder);
        try {
            EMCompilerResult result = EMRTCompiler.compile(clzName+".java",fullCodeStr);
            if(!result.isSuccess()){
                return new EMInvokerCreateResult<>(false,result.getException().getMessage()
                        .replace("EMDynamicInvoker_i_",""),null);
            }

            String clzFullName=result.getResult().keySet().iterator().next();
            Class<?> clz= EMMemoryClassLoader.loadFromBytes(clzFullName,result.getResult());
            if(clz==null){
                return new EMInvokerCreateResult<>(false,"can not load clz:"+clzFullName,null);
            }
            Constructor<?> constructor= clz.getConstructor(String.class);
            Object o=constructor.newInstance(srcCode);
            return new EMInvokerCreateResult<>(true,"", (EMMethodInvoker<T>) o);
        }catch (Exception ex){
            ex.printStackTrace();
            return new EMInvokerCreateResult<>(false,ex.getMessage(),null);
        }
    }

    private String generateCode(String template,Map<String,String> codePlaceHolder){
        String templateCode= ResourceTool.loadResourceAsString(template);
        if(templateCode==null){
            return null;
        }
        for(String key:codePlaceHolder.keySet()){
            templateCode=templateCode.replace(key,codePlaceHolder.get(key));
        }
        return templateCode;
    }


    private  EMObjectGroup<?> findRelationById(long id){
        for(Object ok:context.getOldObjects()){
            EMObjectGroup<?> group=context.getObjectGroup(ok);
            if(group.getId()==id){
                return group;
            }
        }
        return null;
    }

    private EMRTExchange toRTExchange(EMObjectGroup<?> group,boolean includeObject,boolean includeProxy){
        EMRTExchange re=new EMRTExchange();
        if(includeObject){
            re.setMockedObjects(toObjectExchange(group));
        }
        if(includeProxy) {
            re.setProxyInfo(toProxyExchange(group));
        }
        return re;

    }
    private <T> Map<String, List<EMObjectExchange>> toObjectExchange(EMObjectGroup<T> group){
        Map<String, List<EMObjectExchange>> oem=new HashMap<>();//class name and this class's mock object list
        for(Class<? super T> c:group.getMockClass()) {
            List<? extends EMObjectInfo<? super T,?>> el=group.getMockInfo(c);
            oem.computeIfAbsent(c.getName(),k->new ArrayList<>());
            for(EMObjectInfo<? super T,?> e:el) {
                EMObjectExchange oe = new EMObjectExchange();
                oe.setId(e.getId());
                oe.setName(e.getDefinition().getName());
                oe.setOrder(e.getDefinition().getOrder());
                oe.setDefinitionClass(e.getDefinition().getSClass().getName());
                oe.setDefinitionMethod(e.getDefinition().getSMethod().getName());
                oe.setMock(e.isMocked());
                oe.setMethodInfo(toMethodInfo(e));
                oem.get(c.getName()).add(oe);
            }
        }
        return oem;
    }

    private <T> Map<String,List<EMProxyExchange>> toProxyExchange(EMObjectGroup<T> group){
        Map<String,List<EMProxyExchange>> pem=new HashMap<>();
        for(Class<? super T> c:group.getMockClass()){
            EMProxyHolder<? super T> eh=group.getProxyHolder(c);
            EMProxyExchange pe=new EMProxyExchange();
            pe.setProxyClass(eh.getProxy().getClass().getName());
            pe.setInjectField(toStringChain(eh.getInjectField()));
            pem.put(c.getName(), Collections.singletonList(pe));
        }
        return pem;
    }

    private List<String> toStringChain(List<EMFieldInfo> fieldInfos){
        List<String> ls=new ArrayList<>();
        for(EMFieldInfo fi:fieldInfos){
            StringBuilder sb=new StringBuilder(256);
            fi.getFieldTrace().forEach(s->sb.append(" -> ").append(s));
            ls.add(sb.toString());
        }
        return ls;
    }

    private <T> List<EMMethodExchange> toMethodInfo(EMObjectInfo<? super T,?> e){
        List<EMMethodExchange> mes=new ArrayList<>();
        for(String n:e.getInvokeMethods().keySet()){
            EMMethodInfo<?> mi=e.getInvokeMethods().get(n);
            EMMethodExchange me=new EMMethodExchange();
            me.setMethodName(n);
            me.setMock(mi.isMock());
            me.setDynamicInvokeName(mi.getEnabledDynamicInvoker());
            Map<String,EMDynamicInvokeExchange> dim=new HashMap<>();
            for(String in:mi.getDynamicInvokers().keySet()){
                EMMethodInvoker<?> di=mi.getDynamicInvokers().get(in);
                EMDynamicInvokeExchange die=new EMDynamicInvokeExchange();
                die.setSrcCode(di.getCode());
                dim.put(in,die);
            }
            me.setDynamicInvokes(dim);
            mes.add(me);
        }
        return mes;
    }

}
