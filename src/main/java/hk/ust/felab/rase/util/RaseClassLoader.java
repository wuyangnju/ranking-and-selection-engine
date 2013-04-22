package hk.ust.felab.rase.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class RaseClassLoader extends ClassLoader {

	private String className;
	private String classFile;
	private static Set<String> interfaces = new HashSet<String>();

	static {
		interfaces.add("hk.ust.felab.rase.master.Master");
		interfaces.add("hk.ust.felab.rase.ras.Ras");
		interfaces.add("hk.ust.felab.rase.vo.SimInput");
		interfaces.add("hk.ust.felab.rase.vo.SimOutput");
		interfaces.add("hk.ust.felab.rase.sim.Sim");
		interfaces.add("java.lang.Object");
	}

	public RaseClassLoader(String className, String classFile) {
		super();
		this.className = className;
		this.classFile = classFile;
	}

	private static final int BUFFER_SIZE = 8192;

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		// is this class already loaded?
		Class<?> c = findLoadedClass(name);
		if (c != null) {
			return c;
		}

		// does this class need to load?
		if (interfaces.contains(name)) {
			return super.loadClass(name, resolve);
		}

		// get bytes for class
		byte[] classBytes = null;
		try {
			InputStream in = getInputStream(name);
			byte[] buffer = new byte[BUFFER_SIZE];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int n = -1;
			while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
				out.write(buffer, 0, n);
			}
			classBytes = out.toByteArray();
			in.close();
		} catch (IOException e) {
			throw new ClassNotFoundException(e.getMessage());
		}

		if (classBytes == null) {
			throw new ClassNotFoundException("byte array of " + name
					+ "is null");
		}

		// turn the byte array into a Class
		try {
			c = defineClass(name, classBytes, 0, classBytes.length);
			if (resolve) {
				resolveClass(c);
			}
		} catch (SecurityException e) {
			// loading core java classes such as java.lang.String
			// is prohibited, throws java.lang.SecurityException.
			// delegate to parent if not allowed to load class
			c = super.loadClass(name, resolve);
		}

		return c;
	}

	private InputStream getInputStream(String name)
			throws FileNotFoundException {
		if (name.contains(className)) {
			String[] fields = classFile.split("/");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < fields.length - 1; i++) {
				sb.append(fields[i] + "/");
			}
			sb.append(name + ".class");
			return new FileInputStream(sb.toString());
		} else {
			return getResourceAsStream(name.replace('.', '/') + ".class");
		}
	}
}
