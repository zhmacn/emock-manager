package com.mzh.emock.manager.po;

import java.util.List;
import java.util.Map;

public class EMGroupExchange {
    private long id;
    private String oldObject;
    private String oldObjectName;
    private String oldObjectClass;
    private EMRTExchange mockInfo;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOldObject() {
        return oldObject;
    }

    public void setOldObject(String oldObject) {
        this.oldObject = oldObject;
    }

    public String getOldObjectName() {
        return oldObjectName;
    }

    public void setOldObjectName(String oldObjectName) {
        this.oldObjectName = oldObjectName;
    }

    public String getOldObjectClass() {
        return oldObjectClass;
    }

    public void setOldObjectClass(String oldObjectClass) {
        this.oldObjectClass = oldObjectClass;
    }

    public EMRTExchange getMockInfo() {
        return mockInfo;
    }

    public void setMockInfo(EMRTExchange mockInfo) {
        this.mockInfo = mockInfo;
    }

    public static class EMRTExchange{
        private Map<String,List<EMObjectExchange>> mockedObjects;
        private Map<String,List<EMProxyExchange>> proxyInfo;

        public Map<String, List<EMObjectExchange>> getMockedObjects() {
            return mockedObjects;
        }

        public void setMockedObjects(Map<String, List<EMObjectExchange>> mockedObjects) {
            this.mockedObjects = mockedObjects;
        }

        public Map<String, List<EMProxyExchange>> getProxyInfo() {
            return proxyInfo;
        }

        public void setProxyInfo(Map<String, List<EMProxyExchange>> proxyInfo) {
            this.proxyInfo = proxyInfo;
        }
    }
    public static class EMObjectExchange{
        private long id;
        private int order;
        private boolean mock;
        private String name;
        private String definitionClass;
        private String definitionMethod;
        private List<EMMethodExchange> methodInfo;

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public boolean isMock() {
            return mock;
        }

        public void setMock(boolean mock) {
            this.mock = mock;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDefinitionClass() {
            return definitionClass;
        }

        public void setDefinitionClass(String definitionClass) {
            this.definitionClass = definitionClass;
        }

        public String getDefinitionMethod() {
            return definitionMethod;
        }

        public void setDefinitionMethod(String definitionMethod) {
            this.definitionMethod = definitionMethod;
        }

        public List<EMMethodExchange> getMethodInfo() {
            return methodInfo;
        }

        public void setMethodInfo(List<EMMethodExchange> methodInfo) {
            this.methodInfo = methodInfo;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    public static class EMMethodSignatureExchange{
        private long objectId;
        private String methodName;
        private String returnType;
        private String parameterList;
        private String simpleSignature;
        private String importCode;

        public long getObjectId() {
            return objectId;
        }

        public void setObjectId(long objectId) {
            this.objectId = objectId;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public String getParameterList() {
            return parameterList;
        }

        public void setParameterList(String parameterList) {
            this.parameterList = parameterList;
        }

        public String getSimpleSignature() {
            return simpleSignature;
        }

        public void setSimpleSignature(String simpleSignature) {
            this.simpleSignature = simpleSignature;
        }

        public String getImportCode() {
            return importCode;
        }

        public void setImportCode(String importCode) {
            this.importCode = importCode;
        }
    }

    public static class EMMethodExchange{
        private String methodName;
        private boolean mock;
        private String dynamicInvokeName;
        private Map<String,EMDynamicInvokeExchange> dynamicInvokes;

        public boolean isMock() {
            return mock;
        }

        public void setMock(boolean mock) {
            this.mock = mock;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getDynamicInvokeName() {
            return dynamicInvokeName;
        }

        public void setDynamicInvokeName(String dynamicInvokeName) {
            this.dynamicInvokeName = dynamicInvokeName;
        }

        public Map<String, EMDynamicInvokeExchange> getDynamicInvokes() {
            return dynamicInvokes;
        }

        public void setDynamicInvokes(Map<String, EMDynamicInvokeExchange> dynamicInvokes) {
            this.dynamicInvokes = dynamicInvokes;
        }
    }
    public static class EMDynamicInvokeExchange{
        private String srcCode;
        private String addition;

        public String getSrcCode() {
            return srcCode;
        }

        public void setSrcCode(String srcCode) {
            this.srcCode = srcCode;
        }

        public String getAddition() {
            return addition;
        }

        public void setAddition(String addition) {
            this.addition = addition;
        }
    }

    public static class EMProxyExchange{
        private String proxyClass;
        private List<String> injectField;

        public String getProxyClass() {
            return proxyClass;
        }

        public void setProxyClass(String proxyClass) {
            this.proxyClass = proxyClass;
        }

        public List<String> getInjectField() {
            return injectField;
        }

        public void setInjectField(List<String> injectField) {
            this.injectField = injectField;
        }
    }
}
