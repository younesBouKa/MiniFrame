package org.web.core;

import org.web.data.ParamInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public interface RequestParser extends AutoConfigurable{

    /**
     * This method controle parameters and assure parameters order
     * @param methodParameters
     * @param requestParameters
     * @return
     */
    Object[] prepareMethodArgs(Parameter[] methodParameters, Map<String, Object> requestParameters);

    /**
     * Extract parameters values from query, body, header and path
     * @param request
     * @param response
     * @param paramsInfo
     * @return
     * @throws IOException
     */
    Map<String, Object> extractParametersRawValues(HttpServletRequest request, HttpServletResponse response, Set<ParamInfo> paramsInfo, String handlerPath) throws IOException;

    default Map<String, Object> getQueryParams(String requestQuery) throws UnsupportedEncodingException { // requestQuery =  request.getQueryString();
        return parseQuery(requestQuery);
    }

    default Map<String, Object> getBodyParams(HttpServletRequest request) throws IOException {
        Map<String, Object> bodyParams;
        // get params from body (FIXME to see later)
        InputStreamReader isr = new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String body = br.readLine();
        bodyParams = parseQuery(body);
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

    default Map<String, Object> getPathParams(String requestPath, String handlerPath){
        Map<String, Object> pathParams = new HashMap<>();
        if(handlerPath==null || handlerPath.trim().isEmpty()
                || requestPath==null || requestPath.trim().isEmpty())
            return pathParams;
        String[] handlerPathParts = handlerPath.split("//");
        String[] requestPathParts = requestPath.split("//");
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

    default Map<String, Object> parseQuery(String query) throws UnsupportedEncodingException {
        Map<String, Object> parameters = new HashMap<>();
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
        return parameters;
    }

}
