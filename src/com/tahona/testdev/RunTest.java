package com.tahona.testdev;

import junit.framework.Test;

import com.tahona.testdev.suit.DevTestSuit;
public class RunTest {

	public static Test suite() {
		DevTestSuit suit = new DevTestSuit("test/");
		return suit.getTest();
	}

	
	

}
