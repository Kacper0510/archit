package archit.common.stdlib;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ArchitDynamic {
    String name() default "";
}
