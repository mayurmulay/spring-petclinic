package org.springframework.samples.petclinic.profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BeanStartupProfiler implements InstantiationAwareBeanPostProcessor, SmartInitializingSingleton {

	private static final Logger log = LoggerFactory.getLogger(BeanStartupProfiler.class);

	private final Map<String, Long> startTimes = new ConcurrentHashMap<>();

	private final Map<String, Long> durations = new ConcurrentHashMap<>();

	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		startTimes.put(beanName, System.nanoTime());
		return null;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Long start = startTimes.remove(beanName);
		if (start != null) {
			long elapsed = (System.nanoTime() - start) / 1_000_000;
			durations.put(beanName, elapsed);
		}
		return bean;
	}

	@Override
	public void afterSingletonsInstantiated() {
		log.info("");
		log.info("============== Bean Startup Report ==============");

		durations.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.limit(20)
			.forEach(e -> log.info(String.format("%-45s %d ms", e.getKey(), e.getValue())));

		log.info("=================================================");
	}

}