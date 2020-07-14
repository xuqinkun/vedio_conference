package common.bean;

public class HttpResult<T> {
    private ResultCode result;

    private T message;

    public HttpResult() {
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "result='" + result + '\'' +
                ", message='" + message.toString() + '\'' +
                '}';
    }

    public HttpResult(ResultCode result, T message) {
        this.result = result;
        this.message = message;
    }

    public ResultCode getResult() {
        return result;
    }

    public void setResult(ResultCode result) {
        this.result = result;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }
}
