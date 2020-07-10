package common.bean;

public class HttpResult {
    private ResultCode result;

    private String message;

    public HttpResult() {
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "result='" + result + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public HttpResult(ResultCode result, String message) {
        this.result = result;
        this.message = message;
    }

    public ResultCode getResult() {
        return result;
    }

    public void setResult(ResultCode result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
