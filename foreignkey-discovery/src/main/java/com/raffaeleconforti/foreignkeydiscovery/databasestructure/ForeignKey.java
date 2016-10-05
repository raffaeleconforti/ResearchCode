package com.raffaeleconforti.foreignkeydiscovery.databasestructure;

import java.util.TreeSet;

/**
 * Created by Raffaele Conforti on 14/10/14.
 */
public class ForeignKey implements Comparable<ForeignKey>{

    private String nameF;
    private String nameP;
    private TreeSet<Column> columnsF;
    private TreeSet<Column> columnsP;
    private Integer hashCode;

    public ForeignKey(TreeSet<Column> setP, TreeSet<Column> setF) {
        columnsF = new TreeSet<Column>(setF);
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(Column c : columnsF) {
            sb.append(c.getColumnName());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(")");
        nameF = sb.toString();

        columnsP = new TreeSet<Column>(setP);
        sb = new StringBuilder();
        sb.append("(");
        for(Column c : columnsP) {
            sb.append(c.getColumnName());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(")");
        nameP = sb.toString();
    }

    public String getNameF() {
        return nameF;
    }

    public String getNameP() {
        return nameP;
    }

    public TreeSet<Column> getColumnsF() {
    return columnsF;
    }

    public TreeSet<Column> getColumnsP() {
        return columnsP;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof ForeignKey) {
            ForeignKey fk = (ForeignKey) o;
            return columnsF.equals(fk.columnsF) && columnsP.equals(fk.columnsP);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if(hashCode == null) {
            hashCode = columnsF.hashCode() + columnsP.hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString() {

        return "P \n" + columnsP.toString() + "\nF \n" + columnsF.toString();
    }

    @Override
    public int compareTo(ForeignKey o) {

        if(columnsF.size() == o.columnsF.size() && columnsP.size() == o.columnsP.size()) {
            Column[] columnsF1 = columnsF.toArray(new Column[columnsF.size()]);
            Column[] columnsF2 = o.columnsF.toArray(new Column[o.columnsF.size()]);
            Column[] columnsP1 = columnsP.toArray(new Column[columnsP.size()]);
            Column[] columnsP2 = o.columnsP.toArray(new Column[o.columnsP.size()]);
            for(int i = 0; i < columnsP1.length; i++) {
                int compare = columnsP1[i].compareTo(columnsP2[i]);
                if(compare != 0) return compare;
            }
            for(int i = 0; i < columnsF1.length; i++) {
                int compare = columnsF1[i].compareTo(columnsF2[i]);
                if(compare != 0) return compare;
            }
            return 0;
        }
        int compare = Integer.valueOf(columnsP.size()).compareTo(o.columnsP.size());
        if(compare != 0) return compare;
        else return Integer.valueOf(columnsF.size()).compareTo(o.columnsF.size());
    }
}
