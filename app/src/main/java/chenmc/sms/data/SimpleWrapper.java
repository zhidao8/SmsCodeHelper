package chenmc.sms.data;

/**
 * Created by 明明 on 2017/7/21.
 */

public class SimpleWrapper<T, A> {
    public T target;
    public A attr;
    
    public SimpleWrapper(T target, A attr) {
        this.target = target;
        this.attr = attr;
    }
}
