package org.web.core.helpers;

import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.WebConfig;
import org.web.annotations.params.global.ParamSrc;
import org.web.core.ControllerConfig;
import org.web.core.RequestParser;
import org.web.data.ParamInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequestParserImpl implements RequestParser {
    private static final Log logger = Log.getInstance(RequestParserImpl.class);


    @Override
    public void autoConfigure() {

    }

    /**
     * Extract parameters values from query, body, header and path
     * @param request
     * @param response
     * @param paramsInfo
     * @return
     * @throws IOException
     */
    public Map<String, Object> extractParametersRawValues(HttpServletRequest request,
                                                          HttpServletResponse response,
                                                          Set<ParamInfo> paramsInfo,
                                                          String handlerPath) throws IOException {
        Map<String, Object> bodyParams = getBodyParams(request);
        Map<String, Object> queryParams = getQueryParams(request.getQueryString());
        Map<String, Object> pathParams = getPathParams(request.getPathInfo(), handlerPath);
        Map<String, Object> headerParams = getHeaderParams(request);
        // prepare params
        Map<String, Object> neededParams = new HashMap<>();
        String paramName, paramUsedName;
        Object paramValue;
        Class<?> methodParamClass;
        ParamSrc paramSrc;
        ControllerConfig controllerConfig =  WebConfig.getControllerConfig();
        for(ParamInfo methodParamInfo : paramsInfo) {
            paramName = methodParamInfo.getName(); // name in method args
            paramUsedName = methodParamInfo.getUsedName(); // name used in annotation
            methodParamClass = methodParamInfo.getType();
            paramSrc = methodParamInfo.getParamType();
            paramValue = null;
            if(controllerConfig.isInjectableParam(methodParamClass)){
                paramValue =controllerConfig.getRouteInjectedParamValue(request, response, methodParamClass, paramName);
            }else if(ParamSrc.PATH.equals(paramSrc)){
                paramValue = pathParams.get(paramUsedName);
            }else if(ParamSrc.QUERY.equals(paramSrc)){
                paramValue = queryParams.get(paramUsedName);
            }else if(ParamSrc.BODY.equals(paramSrc)){
                paramValue = bodyParams.get(paramUsedName);
            }else if(ParamSrc.HEADER.equals(paramSrc)){
                paramValue = headerParams.get(paramUsedName);
            }else { // same as ParamSrc.BODY
                paramValue = bodyParams.get(paramUsedName);
            }
            if(paramValue==null)
                logger.error("Unknown parameter type: "+methodParamInfo);
            neededParams.put(paramName, paramValue);
        }
        logger.debugF("Extracted Request Parameters: \n" +
                "paramsInfo: %s,\n" +
                "extracted parameters: %s\n",paramsInfo, neededParams );
        return neededParams;
    }

    /**
     * This method controle parameters and assure parameters order
     * @param methodParameters = method.getParameters();
     * @param requestParameters
     * @return
     */
    public Object[] prepareMethodArgs(Parameter[] methodParameters, Map<String, Object> requestParameters){
        //Parameter[] methodParameters = method.getParameters();
        if(methodParameters.length!= requestParameters.size()){
            logger.error("Extracted parameters are not valid\n" +
                    "Method parameters: "+Arrays.asList(methodParameters)+"\n" +
                    "Extracted parameters: "+requestParameters);
            throw new FrameworkException("Extracted parameters are not valid");
        }

        Object[] values = new Object[methodParameters.length];
        int i=0;
        for(Parameter parameter : methodParameters){
            String paramName = parameter.getName();
            if(!requestParameters.containsKey(paramName))
                throw new FrameworkException("Can't find value for parameter ["+ parameter +"]");
            values[i++] = WebConfig
                    .getControllerConfig()
                    .getFormattedValue(requestParameters.get(paramName), parameter.getType());
        }
        return values;
    }
}
