package org.activityinfo.store.mysql;

public class Join {
    private Join parent;
    private String key;
    private String joinExpr;

    public Join(Join parent, String key, String joinExpr) {
        this.parent = parent;
        this.key = key;
        this.joinExpr = joinExpr;
    }

    public Join(String key, String joinExpr) {
        this.key = key;
        this.joinExpr = joinExpr;
    }

    public Join getParent() {
        return parent;
    }

    public String getKey() {
        return key;
    }

    public String getJoinExpr() {
        return joinExpr;
    }
}
