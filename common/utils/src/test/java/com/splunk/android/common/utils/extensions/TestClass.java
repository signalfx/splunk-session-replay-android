package com.splunk.android.common.utils.extensions;

public class TestClass {

	private boolean booleanValue = true;
	private int intValue = 1;
	private long longValue = 1;
	private float floatValue = 1f;
	private double doubleValue = 1.0;
	private String stringValue = "0";
	private InnerClass innerClassValue = new InnerClass();

	private void callVoid() {
	}

	private boolean getBoolean() {
		return booleanValue;
	}

	private int getInt() {
		return intValue;
	}

	private long getLong() {
		return longValue;
	}

	private float getFloat() {
		return floatValue;
	}

	private double getDouble() {
		return doubleValue;
	}

	private String getString() {
		return stringValue;
	}

	private InnerClass getInnerClass() {
		return innerClassValue;
	}

	public static class InnerClass {
	}
}
