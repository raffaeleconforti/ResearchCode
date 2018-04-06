package com.raffaeleconforti.alphadollar.alphamminer;

public class AXYB {
	//4 task names of the mendatious relationship.
	String a;
	String x;
	String y;
	String b;
	int invTasktype; 
	final static String start_a_tag= "START_TASK_A";
	final static String start_x_tag= "END_TASK_X";
	final static String end_y_tag= "END_TASK_Y";
	final static String end_b_tag= "END_TASK_B";
	final static int START = 1;
	final static int END = 2;
	final static int SKIP = 3;
	final static int REDO = 4;
	final static int SWITCH = 5;
	
	public AXYB(String _a, String _x, String _y, String _b, int _type)
	{
		a = _a;
		x = _x;
		y = _y;
		b = _b;
		invTasktype = _type;
	}	
	
	@Override
	public boolean equals(Object _axyb)
	{
		if (!(_axyb instanceof AXYB))
			return false;
		AXYB _a = (AXYB) _axyb;
		if (!this.a.equals(_a.a))
			return false;
		if (!this.b.equals(_a.b))
			return false;
		if (!this.x.equals(_a.x))
			return false;
        return this.y.equals(_a.y);
    }
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" A:"+a);
		sb.append(" B:"+b);
		sb.append(" X:"+x);
		sb.append(" Y:"+y);
		return sb.toString();		
	}
}
