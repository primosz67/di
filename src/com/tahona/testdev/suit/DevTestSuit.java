package com.tahona.testdev.suit;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DevTestSuit {

	private final TestSuite testSuite;
	private String tmpRootPathAcc;

	public DevTestSuit(final String... paths) {
		testSuite = new TestSuite();

		for (final String path : paths) {
			tmpRootPathAcc = path;
			final File file = new File(path);

			if (file.isDirectory()) {
				addTestsFromFiles(file.listFiles());
			} else {
				throw new IllegalArgumentException("Wrong Path! Path should be directory. : "+file.getAbsolutePath());
			}
		}
	}

	private void addTestsFromFiles(final File[] filesOrDirectories) {
		try {
			for (final File file : filesOrDirectories) {
				if (false == file.isDirectory()) {
					addTest(file);
				} else {
					addTestsFromFiles(file.listFiles());
				}
			}
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void addTest(final File file) throws ClassNotFoundException, IOException {
		if (isJavaFile(file)) {
			final String name = getClassName(file);
			final Class<?> clz = Class.forName(name);
			if (isTestCase(clz)) {
				testSuite.addTestSuite(clz.asSubclass(TestCase.class));
			}
		}
	}

	private boolean isJavaFile(final File file) {
		return file.getPath().contains(".java");
	}

	private boolean isTestCase(final Class<?> clz) {
		return clz.getSimpleName().startsWith("Test") && TestCase.class.isAssignableFrom(clz);
	}

	private String getClassName(final File file) {
		String className = file.getPath().replaceAll("\\\\","/").replaceFirst(tmpRootPathAcc, "");
		
		className = className.replace("/", ".");
		className = className.replaceFirst(".", "");
		className = className.replaceFirst(".java", "");
		return className;
	}

	public Test getTest() {
		return testSuite;
	}

}
