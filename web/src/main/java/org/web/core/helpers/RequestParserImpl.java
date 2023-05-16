package org.web.core.helpers;

import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.WebConfig;
import org.web.annotations.params.global.ParamSrc;
import org.web.core.RequestParser;
import org.web.data.ParamInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequestParserImpl implements RequestParser {
    private static final Log logger = Log.getInstance(RequestParserImpl.class);

    /**
     * This method controle parameters and assure parameters order
     * @param method
     * @param parameters
     * @return
     */
    public Object[] formatParamsValues(Method method, Map<String, Object> parameters){
        Parameter[] methodParameters = method.getParameters();
        if(methodParameters.length!= parameters.size()){
            logger.error("Extracted parameters are not valid\n" +
                    "Method parameters: "+Arrays.asList(methodParameters)+"\n" +
                    "Extracted parameters: "+parameters);
            throw new FrameworkException("Extracted parameters are not valid");
        }
        Object[] values = new Object[methodParameters.length];
        int i=0;
        for(Parameter parameter : methodParameters){
            String paramName = parameter.getName();
            if(!parameters.containsKey(paramName))
                throw new FrameworkException("Can't find value for parameter ["+ parameter +"]");
            values[i++] = WebConfig
                    .getControllerConfig()
                    .getFormattedValue(parameters.get(paramName), parameter.getType());
        }
        return values;
    }

    /**
     * Extract parameters values from query, body, header and path
     * @param request
     * @param response
     * @param paramsInfo
     * @return
     * @throws IOException
     */
    public Map<String, Object> extractParametersRawValues(HttpServletRequest request, HttpServletResponse response, Set<ParamInfo> paramsInfo, String handlerPath) throws IOException {
        Map<String, Object> bodyParams = getBodyParams(request);
        Map<String, Object> queryParams = getQueryParams(request);
        Map<String, Object> pathParams = getPathParams(request, handlerPath);
        Map<String, Object> headerParams = getHeaderParams(request);
        // prepare params
        Map<String, Object> neededParams = new HashMap<>();
        String paramName, paramUsedName;
        Object paramValue;
        for(ParamInfo methodParamInfo : paramsInfo) {
            paramName = methodParamInfo.getName(); // name or used name in annotation
            paramUsedName = methodParamInfo.getUsedName(); // name or used name in annotation
            paramValue = WebConfig
                    .getControllerConfig()
                    .getRouteInjectedParamValue(request, response, methodParamInfo.getType(), paramName);
            if(ParamSrc.PATH.equals(methodParamInfo.getParamType())){
                paramValue = pathParams.get(paramUsedName);
            }else if(ParamSrc.QUERY.equals(methodParamInfo.getParamType())){
                paramValue = queryParams.get(paramUsedName);
            }else if(ParamSrc.BODY.equals(methodParamInfo.getParamType())){
                paramValue = bodyParams.get(paramUsedName);
            }else if(ParamSrc.HEADER.equals(methodParamInfo.getParamType())){
                paramValue = headerParams.get(paramUsedName);
            }else if(paramValue==null){
                logger.error("Unknown parameter type: "+methodParamInfo);
            }
            neededParams.put(paramName, paramValue);
        }
        logger.debugF("Extracted Request Parameters: \n" +
                "paramsInfo: %s,\n" +
                "extracted parameters: %s\n",paramsInfo, neededParams );
        return neededParams;
    }

}
