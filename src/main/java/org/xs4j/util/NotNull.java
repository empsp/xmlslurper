package org.xs4j.util;

import java.lang.annotation.*;

/**
 * Created by mturski on 12/22/2016.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER,ElementType.LOCAL_VARIABLE})
public @interface NotNull {
}
