package org.web.core;

import org.web.data.ParamInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public interface RequestParser {

    /**
     * This method controle parameters and assure parameters order
     * @param method
     * @param parameters
     * @return
     */
    public Object[] formatParamsValues(Method method, Map<String, Object> parameters);

    /**
     * Extract parameters values from query, body, header and path
     * @param request
     * @param response
     * @param paramsInfo
     * @return
     * @throws IOException
     */
    Map<String, Object> extractParametersRawValues(HttpServletRequest request, HttpServletResponse response, Set<ParamInfo> paramsInfo, String handlerPath) throws IOException;

    default Map<String, Object> getQueryParams(HttpServletRequest request) throws UnsupportedEncodingException {
        Map<String, Object> queryParams = new HashMap<>();
        String query = request.getQueryString();
        // get params from query
        parseQuery(query, queryParams);
        return queryParams;
    }

    default Map<String, Object> getBodyParams(HttpServletRequest request) throws IOException {
        Map<String, Object> bodyParams = new HashMap<>();
        // get params from body (FIXME to see later)
        InputStreamReader isr = new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String body = br.readLine();
        parseQuery(body, bodyParams);
        return bodyParams;
    }

    default Map<String, Object> getHeaderParams(HttpServletRequest request){
        Map<String, Object> headerParams = new HashMap<>();
        // get headers param
        Enumeration headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String headerName = (String) headerNames.nextElement();
            headerParams.put(headerName, request.getHeader(headerName));
        }
        return headerParams;
    }

    default Map<String, Object> getPathParams(HttpServletRequest request, String handlerPath){
        Map<String, Object> pathParams = new HashMap<>();
        String[] handlerPathParts = handlerPath.split("//");
        String[] requestPathParts = request.getPathInfo().split("//");
        if(handlerPathParts.length != requestPathParts.length)
            return pathParams;
        for(int i=0; i<handlerPathParts.length; i++){
            if(handlerPathParts[i].startsWith("{")){
                String paramName = handlerPathParts[i]
                        .replace("{","")
                        .replace("}","");
                String paramValue = requestPathParts[i];
                pathParams.put(paramName, paramValue);
            }
        }
        return pathParams;
    }

    default boolean validateParameters(Map<String, Object> parametersValues, Set<ParamInfo> paramsInfo){
        // FIXME to see later using validation annotations (see if we can use spec)
        return true;
    }

    default void parseQuery(String query, Map<String,Object> parameters) throws UnsupportedEncodingException {
        if (query != null) {
            String pairs[] = query.split("[&]");
            for (String pair : pairs) {
                String param[] = pair.split("[=]");
                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);

                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }

}
