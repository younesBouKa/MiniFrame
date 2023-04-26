package org.web.data;

public class ResponseWrapper{
    private String contentType = "TEXT"; // TODO more work here for response formatters (xml, json, ...)
    private Object rawResponse;
    public ResponseWrapper(Object rawResponse){
        this.rawResponse = rawResponse;
    }

    public Object getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(Object rawResponse) {
        this.rawResponse = rawResponse;
    }

    public int length(){
        return toString().length();
    }

    public byte[] getBytes(){
        return toString().getBytes();
    }

    @Override
    public String toString() {
        return "ResponseWrapper{" +
                "rawResponse=" + rawResponse +
                '}';
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
