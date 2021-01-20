package com.tradebot.config;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FindEventBusSubscribers implements BeanPostProcessor {

    private final EventBus eventBus;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] beanMethods = bean.getClass().getMethods();
        for (Method beanMethod : beanMethods) {
            if (beanMethod.isAnnotationPresent(Subscribe.class)) {
                eventBus.register(bean);
                log.info(String.format("Found event bus subscriber class %s. Subscriber method name=%s",
                    bean.getClass().getSimpleName(), beanMethod.getName()));
                break;
            }
        }
        return bean;
    }
}
