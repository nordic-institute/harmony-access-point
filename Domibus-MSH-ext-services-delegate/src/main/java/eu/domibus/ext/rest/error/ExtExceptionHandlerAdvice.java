package eu.domibus.ext.rest.error;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @since 4.2
 * @author Catalin Enache
 */
@ControllerAdvice("eu.domibus.ext.rest")
@RequestMapping(produces = "application/vnd.error+json")
public class ExtExceptionHandlerAdvice {

}
