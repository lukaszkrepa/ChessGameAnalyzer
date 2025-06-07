package com.chess.analyzer.backend.mappers;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public interface Mapper<S, T> {
    T mapTo(S source);
    S mapFrom(T source);

    default Collection<T> mapTo(Collection<S> source){
        return source == null ? null : source.stream().map(this::mapTo).toList();
    }
    default Collection<S> mapFrom(Collection<T> source){
        return source == null ? null : source.stream().map(this::mapFrom).toList();
    }

}
