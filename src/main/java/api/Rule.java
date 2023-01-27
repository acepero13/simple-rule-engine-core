package api;

import com.acepero13.research.ruleengine.model.Facts;

public interface Rule extends Comparable<Rule> {
    String name();

    String description();

    int priority();

    boolean evaluates(Facts facts);

    void execute(Facts facts) throws Exception;
}
