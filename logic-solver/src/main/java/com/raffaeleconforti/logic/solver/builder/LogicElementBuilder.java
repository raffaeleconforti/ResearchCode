/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.logic.solver.builder;

import com.raffaeleconforti.logic.solver.elements.*;

import java.util.StringTokenizer;

/**
 * Created by conforti on 6/08/15.
 */
public class LogicElementBuilder {

    private StringTokenizer stringTokenizer;
    private String token;

    public static void main(String[] args) {
        String s = "f(): ( ( ( ( ( ( ( ( ( a OR b ) AND a ) OR b ) AND a ) OR b ) AND a ) OR b ) AND a ) OR b )";
        LogicElementBuilder dnf = new LogicElementBuilder();
        LogicElement e = dnf.parseExpression(s);
        System.out.println(e);
    }

    public LogicElement parseExpression(String booleanExpression) {
        stringTokenizer = new StringTokenizer(booleanExpression, " ");
        return createElement();
    }

    private LogicElement createElement() {
        String token = nextToken();
        if(token.contains(":")) {
            return createLogicFunction();
        }else if(token.equals("(")) {
            return createLogicExpression();
        }else if(token.contains("()")) {
            return createLogicFunctionBookmark();
        }else if(token.equalsIgnoreCase("true") || token.equalsIgnoreCase("false")) {
            return createBooleanElement();
        }else {
            return createAtomicElement();
        }
    }

    private LogicElement createLogicFunction() {
        String name = removeColomnFromFunctionName();
        LogicElement element = createElement();
        return new LogicFunction(name, element);
    }

    private String removeColomnFromFunctionName() {
        return token.substring(0, token.length() - 1);
    }

    private String nextToken() {
        token = stringTokenizer.nextToken();
        return token;
    }

    private LogicExpression createLogicExpression() {
        LogicElement leftElement;
        LogicElement rightElement;
        LogicOperator operator;

        leftElement = createElement();
        operator = createOperator();
        rightElement = createElement();
        nextToken();

        return new LogicExpression(leftElement, operator, rightElement);
    }

    private LogicFunctionBookmark createLogicFunctionBookmark() {
        return new LogicFunctionBookmark(token);
    }

    private LogicElement createBooleanElement() {
        return new BooleanElement(Boolean.parseBoolean(token));
    }

    private LogicElement createAtomicElement() {
        return new AtomicElement(token);
    }

    private LogicOperator createOperator() {
        String token = nextToken();
        if(token.equals("AND")) return LogicOperator.AND;
        else return LogicOperator.OR;
    }



}
