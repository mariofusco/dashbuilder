/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dataset.engine.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.date.TimeFrame;

public class CoreFunction extends DataSetFunction {

    private CoreFunctionFilter coreFunctionFilter = null;

    public CoreFunction(DataSetFilterContext ctx, CoreFunctionFilter coreFunctionFilter) {
        super(ctx, coreFunctionFilter);
        this.coreFunctionFilter = coreFunctionFilter;
    }

    public Comparable getCurrentValue() {
        return (Comparable) getDataColumn().getValues().get(getContext().getCurrentRow());
    }

    public Comparable getParameter(int index) {
        return (Comparable) coreFunctionFilter.getParameters().get(index);
    }

    public boolean pass() {
        CoreFunctionType type = coreFunctionFilter.getType();

        if (CoreFunctionType.IS_NULL.equals(type)) {
            return isNull(getCurrentValue());
        }
        if (CoreFunctionType.NOT_NULL.equals(type)) {
            return isNotNull(getCurrentValue());
        }
        if (CoreFunctionType.EQUALS_TO.equals(type)) {
            return isEqualsTo(getCurrentValue());
        }
        if (CoreFunctionType.NOT_EQUALS_TO.equals(type)) {
            return isNotEqualsTo(getCurrentValue());
        }
        if (CoreFunctionType.LIKE_TO.equals(type)) {
            return isLikeTo(getCurrentValue());
        }
        if (CoreFunctionType.LOWER_THAN.equals(type)) {
            return isLowerThan(getCurrentValue());
        }
        if (CoreFunctionType.LOWER_OR_EQUALS_TO.equals(type)) {
            return isLowerThanOrEqualsTo(getCurrentValue());
        }
        if (CoreFunctionType.GREATER_THAN.equals(type)) {
            return isGreaterThan(getCurrentValue());
        }
        if (CoreFunctionType.GREATER_OR_EQUALS_TO.equals(type)) {
            return isGreaterThanOrEqualsTo(getCurrentValue());
        }
        if (CoreFunctionType.BETWEEN.equals(type)) {
            return isBetween(getCurrentValue());
        }
        if (CoreFunctionType.TIME_FRAME.equals(type)) {
            return timeFrame(getCurrentValue());
        }
        throw new IllegalArgumentException("Core function type not supported: " + type);
    }

    public boolean isNull(Comparable value) {
        return value == null;
    }

    public boolean isNotNull(Comparable value) {
        return !isNull(value);
    }

    public boolean isEqualsTo(Comparable value) {
        if (isNull(value)) return false;

        Comparable ref = getParameter(0);
        return ref.equals(value);
    }

    public boolean isNotEqualsTo(Comparable value) {
        return !isEqualsTo(value);
    }

    /**
     * <p>The <code>LIKE_TO</code> operator is intended to emulate the SQL like operator.It's used to search for a specified pattern in a data set's column.</p>
     * <p>Allowed wildcards are:</p>
     * <ul>
     *     <li><code>_</code> - A substitute for a single character.</li>
     *     <li><code>%</code> - A substitute for zero or more characters.</li>
     * </ul>
     * <p>The implementation is supported for TEXT or LABEL column types and considers case UNSENSITIVE.</p>
     *
     * <p>The call <code>getParameter(0)</code> returns the given user's input pattern used for searching.</p>
     * @param value The existing data set column's value at a given row.
     * 
     * @return If the string on current data set's column is like (as the SQL operator) the given user's pattern.
     */
    public boolean isLikeTo(Comparable value) {
        if (isNull(value)) return false;
        final Comparable _pattern = getParameter(0);
        if (isNull(_pattern)) return false;
        String existingColumnValue = value.toString();
        String pattern = getParameter(0).toString();
        if (isEmpty(existingColumnValue) && isEmpty(pattern)) return true;
        
        // Replace the user's wilcards that come from the UI request for valid regular expression patterns.
        pattern = pattern.toLowerCase(); // ignoring case
        pattern = pattern.replace(".", "\\."); // "\\" is escaped to "\"
        // pattern = pattern.replace("?", ".");
        // pattern = pattern.replace("*", ".*");
        pattern = pattern.replace("%", ".*");
        pattern = pattern.replace("_", ".");
        pattern = pattern.toLowerCase();
        
        // Perform the regexp match operation.
        return existingColumnValue.toLowerCase().matches(pattern);
    }
    
    public boolean isLowerThan(Comparable value) {
        if (isNull(value)) return false;

        Comparable ref = getParameter(0);
        return value.compareTo(ref) == -1;
    }

    public boolean isLowerThanOrEqualsTo(Comparable value) {
        if (isNull(value)) return false;

        Comparable ref = getParameter(0);
        return value.compareTo(ref) != 1;
    }

    public boolean isGreaterThan(Comparable value) {
        if (isNull(value)) return false;

        Comparable ref = getParameter(0);
        return value.compareTo(ref) == 1;
    }

    public boolean isGreaterThanOrEqualsTo(Comparable value) {
        if (isNull(value)) return false;

        Comparable ref = getParameter(0);
        return value.compareTo(ref) != -1;
    }

    public boolean isBetween(Comparable value) {
        if (isNull(value)) return false;

        Comparable low = getParameter(0);
        Comparable high = getParameter(1);
        if (value.compareTo(low) == -1) return false;
        if (value.compareTo(high) == 1) return false;
        return true;
    }

    Map<String, TimeFrameLimits> _timeFrameExprCache = new HashMap<String, TimeFrameLimits>();

    public boolean timeFrame(Comparable value) {
        if (isNull(value)) return false;
        if (!(value instanceof Date)) return false;
        Date target = (Date) value;

        String timeFrameExpr = getParameter(0).toString();
        TimeFrameLimits timeFrameLimits = _timeFrameExprCache.get(timeFrameExpr);
        if (timeFrameLimits == null) {
            TimeFrame timeFrame = TimeFrame.parse(getParameter(0).toString());
            _timeFrameExprCache.put(timeFrameExpr, timeFrameLimits = new TimeFrameLimits(timeFrame));
        }

        if (target.before(timeFrameLimits.from)) return false;
        if (target.after(timeFrameLimits.to)) return false;
        return true;
    }

    public class TimeFrameLimits {

        Date from = null;
        Date to = null;

        public TimeFrameLimits(TimeFrame timeFrame) {
            from = timeFrame.getFrom().getTimeInstant();
            to = timeFrame.getTo().getTimeInstant();
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }
}
