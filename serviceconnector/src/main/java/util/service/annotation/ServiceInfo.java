package util.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a IInterface field to be initialized by {@link util.service.ServiceConnector} with the
 * remove service object.
 * <p>
 * This should specify the intent to be used to connect to the remote service. This object will
 * get initialized with the service object from the binder returned by the service.
 * <p>
 * ex:
 * <pre>
 * {@code
 *
 *  @literal @ServiceInfo(serviceIntent="com.myintent.MY_SERVICE")
 *   private IMYService myService;
 * }
 * </pre>
 *
 * @author jsam
 * @see util.service.ServiceConnector
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ServiceInfo {

    String serviceIntent();
}
