package com.jo0oy.springbatchpractice.part3;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@RequiredArgsConstructor
public class DuplicateValidateProcessor<T> implements ItemProcessor<T, T> {
    private static Map<String, Object> keyPool = new ConcurrentHashMap<>();
    private final Function<T, String> keyExtractor;
    private final boolean allowDuplicate;

    @Override
    public T process(T item) throws Exception {

        if (allowDuplicate) {
            return item;
        }

        String key = keyExtractor.apply(item);

        if (keyPool.containsKey(key)) {
            return null;
        }

        keyPool.put(key, key);
        return item;
    }
}
