package org.web.remoting;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.SerializableEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.tools.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceInvoker<T> implements InvocationHandler {
    private static final Log logger = Log.getInstance(ServiceInvoker.class);
    private static final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    private final String serviceURL;
    private final Class<?> serviceInterface;

    private ServiceInvoker(String serviceURL, Class<?> serviceInterface) {
        this.serviceURL = serviceURL;
        this.serviceInterface = serviceInterface;
    }

    public static Object newInstance(String serviceURL, Class<?> serviceInterface) {
        return java.lang.reflect.Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                new ServiceInvoker<>(serviceURL, serviceInterface));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        String methodSignature = method.toGenericString();
        Class<?> methodReturnType = method.getReturnType();
        // configured client instance
        try (CloseableHttpClient httpClient = clientBuilder.build()) {
            ClassicHttpRequest httpPost = ClassicRequestBuilder.post(serviceURL)
                    .setEntity(new SerializableEntity(args, ContentType.DEFAULT_BINARY))
                    .addHeader("serviceInterface", serviceInterface.getCanonicalName())
                    .addHeader("serviceUrl", serviceURL)
                    .addHeader("methodSignature", methodSignature)
                    .build();
            result = httpClient.execute(httpPost, response -> {
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                final HttpEntity responseEntity = response.getEntity();
                try (ObjectInputStream objectInputStream = new ObjectInputStream(responseEntity.getContent())) {
                    return methodReturnType.cast(objectInputStream.readObject());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    EntityUtils.consume(responseEntity);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static void main(String[] args) {

    }
}
