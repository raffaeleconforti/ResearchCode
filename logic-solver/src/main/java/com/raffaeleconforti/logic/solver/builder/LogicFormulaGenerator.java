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

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;

/**
 * Created by conforti on 8/08/15.
 */
public class LogicFormulaGenerator<T> {

    private Automaton<T> automaton;

    public static void main(String[] args) {
        Automaton<String> automaton = new Automaton<String>();

        Node<String> s1 = new Node<String>("s1");
        Node<String> s2 = new Node<String>("s2");
        Node<String> s3 = new Node<String>("s3");
        Node<String> s4 = new Node<String>("s4");

        automaton.addNode(s1);
        automaton.addNode(s2);
        automaton.addNode(s3);
        automaton.addNode(s4);

        Edge<String> a = new Edge<String>(s1, s2);
//        a.setInfrequent(true);
        Edge<String> b = new Edge<String>(s2, s4);
//        b.setInfrequent(true);
        Edge<String> c = new Edge<String>(s3, s4);
//        c.setInfrequent(true);
        Edge<String> d = new Edge<String>(s3, s2);
//        d.setInfrequent(true);
        Edge<String> e = new Edge<String>(s1, s3);
        Edge<String> f = new Edge<String>(s2, s3);
//
        automaton.addEdge(a);
        automaton.addEdge(b);
        automaton.addEdge(c);
        automaton.addEdge(d);
        automaton.addEdge(e);
        automaton.addEdge(f);

        LogicFormulaGenerator<String> lfg = new LogicFormulaGenerator<String>(automaton);
        System.out.println(lfg.generateInputFormulas());
        System.out.println(lfg.generateOutputFormulas());
    }

    public LogicFormulaGenerator(Automaton<T> automaton) {
        this.automaton = automaton;
    }

    public String generateInputFormulas() {
        StringBuffer stringBuffer = new StringBuffer();

        for(Node<T> node : automaton.getNodes()) {
            stringBuffer.append(generateInputFormulas(node));
            stringBuffer.append("\n");
        }

        return stringBuffer.toString();
    }

    public String generateOutputFormulas() {
        StringBuffer stringBuffer = new StringBuffer();

        for(Node<T> node : automaton.getNodes()) {
            stringBuffer.append(generateOutputFormulas(node));
            stringBuffer.append("\n");
        }

        return stringBuffer.toString();
    }

    private String generateInputFormulas(Node<T> node) {
        StringBuffer stringBuffer = new StringBuffer();

        int addParenthesysCount = 0;
        for(Edge<T> edge : automaton.getEdges()) {
            if(edge.getTarget().equals(node)) {
                if(addParenthesysCount == 1) {
                    stringBuffer.append(" OR ");
                }else if(addParenthesysCount > 1) {
                    addParenthesysForBonaryFormat(stringBuffer);
                    stringBuffer.append(" ) OR ");
                }

                stringBuffer.append(generateInputExpression(edge));
                addParenthesysCount++;
            }
        }

        if(stringBuffer.toString().contains(" OR ")) {
            addParenthesysForBonaryFormat(stringBuffer);
            stringBuffer.append(" )");
        }else if(stringBuffer.length() == 0){
            stringBuffer.append("TRUE");
        }

        addInputFunctionSymbol(stringBuffer, node);

        return stringBuffer.toString();
    }

    private String generateOutputFormulas(Node<T> node) {
        StringBuffer stringBuffer = new StringBuffer();

        int addParenthesysCount = 0;
        for(Edge<T> edge : automaton.getEdges()) {
            if(edge.getSource().equals(node)) {
                if(addParenthesysCount == 1) {
                    stringBuffer.append(" OR ");
                }else if(addParenthesysCount > 1) {
                    addParenthesysForBonaryFormat(stringBuffer);
                    stringBuffer.append(" ) OR ");
                }

                stringBuffer.append(generateOutputExpression(edge));
                addParenthesysCount++;
            }
        }

        if(stringBuffer.toString().contains(" OR ")) {
            addParenthesysForBonaryFormat(stringBuffer);
            stringBuffer.append(" )");
        }else if(stringBuffer.length() == 0){
            stringBuffer.append("TRUE");
        }

        addOutputFunctionSymbol(stringBuffer, node);

        return stringBuffer.toString();
    }

    private void addInputFunctionSymbol(StringBuffer stringBuffer, Node<T> node) {
        String s = node.getData() + "()i: ";
        stringBuffer.insert(0, s);
    }

    private void addOutputFunctionSymbol(StringBuffer stringBuffer, Node<T> node) {
        String s = node.getData() + "()o: ";
        stringBuffer.insert(0, s);
    }

    private void addParenthesysForBonaryFormat(StringBuffer stringBuffer) {
        String s = "( ";
        stringBuffer.insert(0, s);
    }

    private String generateInputExpression(Edge<T> edge) {
        if(edge.isInfrequent()) {
            return "( " + edge.getSource().getData() + "()i AND " + edge.getId() + " )";
        }else {
            return edge.getSource().getData() + "()i";
        }
    }

    private String generateOutputExpression(Edge<T> edge) {
        if(edge.isInfrequent()) {
            return "( " + edge.getTarget().getData() + "()o AND " + edge.getId() + " )";
        }else {
            return edge.getTarget().getData() + "()o";
        }
    }

}
