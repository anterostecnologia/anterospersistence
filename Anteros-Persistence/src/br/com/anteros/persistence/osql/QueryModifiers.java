package br.com.anteros.persistence.osql;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Objects;

public final class QueryModifiers implements Serializable{

    public static final QueryModifiers EMPTY = new QueryModifiers();

    private static int toInt(Long l) {
        if (l.longValue() <= Integer.MAX_VALUE) {
            return l.intValue();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public static QueryModifiers limit(long limit) {
        return new QueryModifiers(Long.valueOf(limit), null);
    }

    public static QueryModifiers offset(long offset) {
        return new QueryModifiers(null, Long.valueOf(offset));
    }

    private final Long limit, offset;

    private QueryModifiers() {
        limit = null;
        offset = null;
    }

    public QueryModifiers(Long limit, Long offset) {
        this.limit = limit;
        if (limit != null && limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0.");
        }
        this.offset = offset;
        if (offset != null && offset < 0) {
            throw new IllegalArgumentException("Offset must not be negative.");
        }
    }

    public QueryModifiers(QueryModifiers modifiers) {
        this.limit = modifiers.getLimit();
        this.offset = modifiers.getOffset();
    }

    public Long getLimit() {
        return limit;
    }

    public Integer getLimitAsInteger() {
        return limit != null ? toInt(limit) : null;
    }

    public Long getOffset() {
        return offset;
    }

    public Integer getOffsetAsInteger() {
        return offset != null ? toInt(offset) : null;
    }

    public boolean isRestricting() {
        return limit != null || offset != null;
    }

    public <T> List<T> subList(List<T> list) {
        if (!list.isEmpty()) {
            int from = offset != null ? toInt(offset) : 0;
            int to = limit != null ? (from + toInt(limit)) : list.size();
            return list.subList(from, Math.min(to,list.size()));
        } else {
            return list;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof QueryModifiers) {
            QueryModifiers qm = (QueryModifiers)o;
            return Objects.equal(qm.getLimit(), limit) && Objects.equal(qm.getOffset(), offset);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(limit, offset);
    }

}
