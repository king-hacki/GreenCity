package greencity.aspects;

import greencity.annotations.EventPublishing;
import greencity.constant.ErrorMessage;
import greencity.events.CustomApplicationEvent;
import greencity.exception.exceptions.EventCreationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Aspect used for publishing events.
 */
@Aspect
@Component
public class EventPublishingAspect {
    private ApplicationEventPublisher publisher;

    /**
     * All args constructor.
     *
     * @param publisher object, used for publishing events.
     */
    @Autowired
    public EventPublishingAspect(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * pointcut, that selects method, annotated with {@link EventPublishing}.
     */
    @Pointcut("@annotation(eventAnnotation)")
    public void myAnnotationPointcut(EventPublishing eventAnnotation) {
        // This method is empty because method with @Pointcut annotation should be empty.
    }

    /**
     * advice, that builds and publishes events.
     *
     * @param eventAnnotation annotation, that is over method, that triggered events publishing.
     * @param returnObject    object, that triggered events method returns.
     */
    @AfterReturning(pointcut = "myAnnotationPointcut(eventAnnotation)", returning = "returnObject")
    public void eventPublishingAdvice(JoinPoint jp, EventPublishing eventAnnotation, Object returnObject) {
        for (Class<? extends CustomApplicationEvent> eventClass : eventAnnotation.eventClass()) {
            publisher.publishEvent(buildEvent(eventClass, returnObject,
                jp.getTarget()));
        }
    }

    /**
     * Method for creating instance of events.
     *
     * @param eventClass class of needed events.
     * @param body       data, that will be put into events.
     * @param source     the object on which the events initially occurred (never {@code null}).
     * @return instance instance of events.
     */
    private ApplicationEvent buildEvent(
        Class<? extends CustomApplicationEvent> eventClass, Object body, Object source) {
        try {
            return eventClass.getConstructor(Object.class, body.getClass()).newInstance(source, body);
        } catch (ReflectiveOperationException e) {
            try {
                return eventClass.getConstructor(Object.class).newInstance(source);
            } catch (ReflectiveOperationException ex) {
                throw new EventCreationException(ErrorMessage.CAN_NOT_CREATE_EVENT_INSTANCE);
            }
        }
    }
}