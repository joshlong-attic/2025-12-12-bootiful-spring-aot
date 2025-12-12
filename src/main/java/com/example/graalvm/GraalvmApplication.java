package com.example.graalvm;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;


@RegisterReflectionForBinding(Customer.class)
@SpringBootApplication
public class GraalvmApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraalvmApplication.class, args);
    }

    @Bean
    static MyBeanPostProcessor myBeanPostProcessor() {
        return new MyBeanPostProcessor();
    }

    @Bean
    static MyBeanFactoryPostProcessor myBeanFactoryPostProcessor() {
        return new MyBeanFactoryPostProcessor();
    }

    @Bean
    static MyBeanFactoryInitializationAotProcessor aotBeanFactoryInitializationAotProcessor() {
        return new MyBeanFactoryInitializationAotProcessor();
    }
}

class MyBeanFactoryInitializationAotProcessor implements BeanFactoryInitializationAotProcessor {

    private final ClassPathScanningCandidateComponentProvider pathScanningCandidateComponentProvider =
            new ClassPathScanningCandidateComponentProvider(false);


    @Override
    public @Nullable BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
        return (generationContext, code) -> {
            var hints = generationContext.getRuntimeHints();
//                hints.reflection().registerType()
        };
    }
}

@ImportRuntimeHints(MessageLoadingThing.Hints.class)
@Component
class MessageLoadingThing {

    private static final Resource MESSAGE = new ClassPathResource("/message");


    @EventListener
    void on(ApplicationReadyEvent event) throws IOException {
        IO.println("message: " + MESSAGE.getContentAsString(Charset.defaultCharset()));
    }

    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
            hints.reflection().registerType(Customer.class, MemberCategory.values());
            hints.serialization().registerType(Cart.class);
            hints.resources().registerResource(MESSAGE);
        }
    }
}

@Component
class Cart implements Serializable {
}
// throws away:
// - jni
// - reflection
// - jdk proxies
// - resource loading


@Component
class Foo {

    Foo() {
        IO.println("foo");
    }
}

class MyBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor, BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//       registry.removeBeanDefinition("foo");

    }

    @Override
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {

        for (var beanName : beanFactory.getBeanDefinitionNames()) {

            var beanDefinition = beanFactory.getBeanDefinition(beanName);

            var clzz = beanFactory.getType(beanName);
            IO.println("beanDefinition: " + beanDefinition.getScope() + ":" +
                    clzz);


        }

    }
}

// (ingest) component scanning, java config, beanRegistrar ...
// BeanDefinitions  (BeanFactoryPostProcessor)
// beans (BeanpostProcessors)

interface Tx {
}

class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        IO.println("postProcessAfterInitialization " + beanName);
        return bean;

    }
}


record Customer(int id, String name) {
}

interface CustomerService {

    Collection<Customer> getCustomers();
}

/*
class TransactionCustomerService implements CustomerService {

    private final CustomerService target;

    TransactionCustomerService(CustomerService target) {
        this.target = target;
    }

    @Override
    public Collection<Customer> getCustomers() {
        // start
        return List.of();
        // stop
    }
}*/

@Service
class JdbcCustomerService implements CustomerService {

    @Override
    public Collection<Customer> getCustomers() {
        return List.of();
    }
// talks to jdbc

}

// @Transactional

class MyComponent {
}