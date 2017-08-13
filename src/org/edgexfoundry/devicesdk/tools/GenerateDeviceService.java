/*******************************************************************************
 * Copyright 2016-2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @microservice:  device-sdk-tools
 * @author: Tyler Cox, Dell
 * @version: 1.0.0
 *******************************************************************************/
package org.edgexfoundry.devicesdk.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GenerateDeviceService
{
	private static final String SERVICE_NAME = "Service name";
	private static final String PROTOCOL_NAME = "Protocol name";
	private static final String PROTOCOL_PATH = "Protocol path";
	private static final String PACKAGE = "Package";

	private static final String PROFILE_ATTRIBUTE = "Profile attribute";
	private static final String RESOURCES = "/src/main/resources/";
	private static final String TEST_RESOURCES = "/src/test/resources/";
	private static final String TEST_SOURCE = "/src/test/java/";

	private static Map<String, String> parameters = new HashMap<>();
	private static Map<String, Boolean> flags = new HashMap<>();

	public static void main(String[] args) throws IOException
	{
		if (args.length != 2)
		{
			System.out.println(
					"Usage: GenerateDeviceService <path> <configuration file>");
			System.exit(1);
		}
		readConfiguration(args[1]);
		generate(args[0] + "/" + parameters.get(SERVICE_NAME));
		System.out.println("Device Service generated successfully");
		System.exit(0);
	}

	protected static void generate(String path) throws IOException
	{
		String subPath = parameters.get(PACKAGE).replaceAll("[.]", "/");
		String fullPath = path + "/src/main/java/" + subPath;
		createDirectories(path);
		createSourceDirectories(fullPath);
		generateApplication(fullPath);
		generateJavaClass(fullPath, "BaseService");
		generateJavaClass(fullPath, "Initializer");
		generateJavaClass(fullPath, "HeartBeat");
		generateJavaClass(fullPath + "/controller", "CommandController");
		generateJavaClass(fullPath + "/controller", "LocalErrorController");
		generateJavaClass(fullPath + "/controller", "ScheduleController");
		generateJavaClass(fullPath + "/controller", "ServiceController");
		generateJavaClass(fullPath + "/controller", "StatusController");
		generateJavaClass(fullPath + "/controller", "UpdateController");
		generateJavaClass(fullPath + "/data", 		"DeviceStore");
		generateJavaClass(fullPath + "/data", 		"ObjectStore");
		generateJavaClass(fullPath + "/data", 		"ProfileStore");
		generateJavaClass(fullPath + "/data", 		"WatcherStore");
		generateProtocolAttributes(fullPath);
		generateJavaClass(fullPath + "/domain",		"ProtocolObject");
		generateJavaClass(fullPath + "/domain",		"ResponseObject");
		generateJavaClass(fullPath + "/domain",		"ScanList");
		generateJavaClass(fullPath + "/domain",		"SimpleSchedule");
		generateJavaClass(fullPath + "/domain",		"SimpleScheduleEvent");
		generateJavaClass(fullPath + "/domain",		"SimpleWatcher");
		generateJavaClass(fullPath + "/domain",		"Transaction");
		generateJavaClass(fullPath + "/handler", 	"CommandHandler");
		generateJavaClass(fullPath + "/handler", 	"CoreDataMessageHandler");
		generateJavaClass(fullPath + "/handler", 	"ProtocolHandler");
		generateJavaClass(fullPath + "/handler", 	"SchedulerCallbackHandler");
		generateJavaClass(fullPath + "/handler",	"UpdateHandler");
		String protocol = "/" + parameters.get(PROTOCOL_PATH);
		generateJavaClass(fullPath + protocol,		"DeviceDiscovery");
		generateJavaClass(fullPath + protocol,		"ObjectTransform");
		generateJavaClass(fullPath + protocol,		"ProtocolDriver");
		generateJavaClass(fullPath + "/scheduling",	"ScheduleContext");
		generateJavaClass(fullPath + "/scheduling",	"ScheduleEventExecutor");
		generateJavaClass(fullPath + "/scheduling",	"ScheduleEventHttpExecutor");
		generateJavaClass(fullPath + "/scheduling",	"Scheduler");
		generatePropertiesFiles(path);
		generateProjectFiles(path);
	}

	protected static void readConfiguration(String string) throws IOException
	{
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(string));
			String line;
			while ((line = in.readLine()) != null)
			{
				if (!line.startsWith("#") && line.indexOf('=') >= 0)
				{
					String[] split = line.split("=");
					String key = split[0].trim();
					String value = split[1].trim();
					if (!value.toLowerCase().equals("true") && !value.toLowerCase().contains("false")) {
						parameters.put(key, value);
						if(key.equals(PACKAGE)) {
							// If the package is not the EdgeX default, add a component scan line to spring-config.xml
							if (!value.equals("org.edgexfoundry")) {
								String BasePackage = value + "\" />\n\t<context:component-scan base-package=\"org.edgexfoundry";
								parameters.put("Base" + key, BasePackage);
							} else {
								parameters.put("Base" + key, value);
							}
						}
					} else {
						flags.put(key, Boolean.valueOf(value));
					}
				}
			}
			parameters.put(PROTOCOL_PATH,parameters.get(PROTOCOL_NAME).toLowerCase());
		} finally {
			if (in != null)
				in.close();
		}
	}

	private static void createDirectories(String path)
	{
		new File(path + RESOURCES).mkdirs();
		new File(path + TEST_RESOURCES).mkdirs();
		new File(path + TEST_SOURCE).mkdirs();
		try {
			new File(path + TEST_RESOURCES + ".gitkeep").createNewFile();
			new File(path + TEST_SOURCE + ".gitkeep").createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private static void createSourceDirectories(String fullPath) {
		new File(fullPath).mkdirs();
		new File(fullPath + "/controller").mkdirs();
		new File(fullPath + "/data").mkdirs();
		new File(fullPath + "/domain").mkdirs();
		new File(fullPath + "/handler").mkdirs();
		new File(fullPath + "/" + parameters.get(PROTOCOL_PATH)).mkdirs();
		new File(fullPath + "/scheduling").mkdirs();
	}
	
	private static void generateProjectFiles(String path) throws IOException
	{
		final File folder = new File("project/");
	    for (final File fileEntry : folder.listFiles()) {
	        if (!fileEntry.isDirectory()) {
	        	generateFromTemplate(path + "/" + fileEntry.getName(), "project/" + fileEntry.getName(), null);
	        }
	    }
	}

	private static void generateApplication(String path) throws IOException
	{
		generateFromTemplate(path + "/Application.java",
				"templates/Application.java");
	}
	
	/**
	 * Generate device object class. The device object has mandatory and
	 * optional fields as defined in <b>DeviceObject</b> class, and it can also
	 * have custom fields as defined in the code generator configuration file.
	 * If a custom field is defined for any device object it will be placed into
	 * this class. All custom fields are optional for any device object, custom
	 * field does not have to appear in all objects for that profile.
	 *
	 * @param path
	 * @throws IOException
	 */
	private static void generatePropertiesFiles(String path) throws IOException
	{
		final File folder = new File("templates/");
	    for (final File fileEntry : folder.listFiles()) {
	        if (!fileEntry.isDirectory() && !fileEntry.getName().contains(".java")) {
	        	String newFile = fileEntry.getName().replace("Protocol", parameters.get(PROTOCOL_NAME));
	        	newFile = fileEntry.getName().replace("ServiceName", parameters.get(SERVICE_NAME));
	        	if(fileEntry.getName().contains(".raml")) {
	        		generateFromTemplate(path + TEST_RESOURCES + newFile, "templates/" + fileEntry.getName(), null);
	        	} else {
	        		generateFromTemplate(path + RESOURCES + newFile, "templates/" + fileEntry.getName(), null);
	        	}
	        }
	    }
	}

	/**
	 * Generate protocol attributes class. The protocol attribute has mandatory and
	 * optional fields as defined in <b>ProfileAttribute</b> class, and it can also
	 * have custom fields as defined in the code generator configuration file.
	 * If a custom field is defined for any device object it will be placed into
	 * this class. All custom fields are optional for any device object, custom
	 * field does not have to appear in all objects for that profile.
	 *
	 * @param path
	 * @throws IOException
	 */
	private static void generateProtocolAttributes(String path) throws IOException
	{
		TreeMap<String, String> customs = new TreeMap<>();
		for (int custom = 1; parameters
				.containsKey(PROFILE_ATTRIBUTE + custom); custom++)
		{
			String[] split = parameters.get(PROFILE_ATTRIBUTE + custom)
					.split("[,]");
			customs.put(split[0], split[1]);
		}
		Map<String, String> subs = new HashMap<>();
		subs.put("CustomAttributes", generateCustomAttributes(customs));
		subs.put("GetCustomAttributes", generateGetCustomAttributes(customs));
		subs.put("SetCustomAttributes", generateSetCustomAttributes(customs));
		subs.put("CustomAttributesInitialization", generateInitializeCustomAttributes(customs));
		generateFromTemplate(path + "/domain/" + parameters.get(PROTOCOL_NAME)
				+ "Attribute.java", "templates/ProtocolAttribute.java", subs);
	}

	private static void generateFromTemplate(String path, String template)
			throws IOException
	{
		generateFromTemplate(path, template, null);
	}
	
	private static String replaceParameters(String line, Map<String, String> subs) {
		String o = "";
		int pos = line.indexOf("${");
		int next = line.indexOf("${", pos + 1);
		int firstTerm = line.indexOf('}', pos + 1);
		while (next >= 0 && firstTerm > next) {
			pos = next;
			next = line.indexOf("${", pos + 1);
		}
		while (pos >= 0)
		{
			o += line.substring(0, pos);
			line = line.substring(pos + 2);
			int pos2 = line.indexOf('}');
			if (pos2 >= 0)
			{
				String key = line.substring(0, pos2);
				line = line.substring(pos2 + 1);
				pos2 = key.indexOf('#');
				String param = parameters.get(key);
				if (param == null && subs != null)
				{
					param = subs.get(key);
				}
				// if parameter is not found in all our maps, assume it is a
				// string that needs to be passed through as-is, including
				// the ${}, there are plenty of those with @Value
				// annotations
				if (param == null)
				{
					param = "${" + key + "}";
				}
				o += param;
			}
			pos = line.indexOf("${");
		}
		o += line;
		return o;
	}

	/**
	 * Generate an output file using a template. Template can have variables in
	 * format ${variable name} that will be expanded using values from the
	 * configuration file. Variable names can contain one '#' letter that will
	 * be replaced with the index value for the parameter lookup.
	 *
	 * @param path
	 *            Full output path for the generated file.
	 * @param template
	 *            Template file name from /templates folder.
	 * @param index
	 *            Index value to use for numbered parameter lookup.
	 * @param subs
	 *            List of substitutions. If variable is not found in the
	 *            configuration file, this list is used to find the value.
	 * @throws IOException
	 *             I/O error.
	 */
	private static void generateFromTemplate(String path, String template,
			Map<String, String> subs) throws IOException
	{
		PrintStream out = null;
		BufferedReader in = null;
		Boolean wroteContent = false;
		try {
			out = new PrintStream(path);
			in = new BufferedReader(
				new FileReader(template));
			String line;
			InterpretedFlags flagged = new InterpretedFlags();
			while ((line = in.readLine()) != null)
			{
				flagged = checkFlags(line, flagged);
				if (!flagged.flagged.equals("") || !flagged.thisLine) continue;
				String o = replaceParameters(line, subs);
				write(out, o + "\n");
				wroteContent = true;
			}
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (!wroteContent)
				new File(path).delete();
		}
	}
	
	private static InterpretedFlags checkFlags(String line, InterpretedFlags flagged) {
		flagged.thisLine = true;
		for(String flag: flags.keySet()) {
			if(line.contains(flag)) {
				flagged.thisLine = false;
				if(!flags.get(flag) && line.contains("<--")) {
					
					flagged.flagged = flagged.flagged + flag + " ";
				}
				if(!flags.get(flag) && line.contains("-->") && flagged.flagged.contains(flag)) {
					flagged.flagged = flagged.flagged.replaceAll(flag + " ", "");
				}
			}
		}
		return flagged;
	}
	
	private static class InterpretedFlags {
	    boolean thisLine = false;
	    String flagged = "";
	    
	    public InterpretedFlags() {
	    	
	    }
	}


	private static String generateCustomAttributes(
			TreeMap<String, String> customs)
	{
		StringBuilder out = new StringBuilder(1000);
		for (String name : customs.keySet())
		{
			out.append("\n\tprivate " + customs.get(name) + " " + name + ";");
		}
		return out.toString();
	}

	private static String generateGetCustomAttributes(
			TreeMap<String, String> customs)
	{
		StringBuilder out = new StringBuilder(1000);
		for (String name : customs.keySet())
		{
			out.append("\n\tpublic " + customs.get(name) + " get"
					+ name.substring(0, 1).toUpperCase() + name.substring(1)
					+ "()\n");
			out.append("\t{\n");
			out.append("\t\treturn " + name + ";\n");
			out.append("\t}");
		}
		return out.toString();
	}
	
	private static String generateSetCustomAttributes(
			TreeMap<String, String> customs)
	{
		StringBuilder out = new StringBuilder(1000);
		for (String name : customs.keySet())
		{
			out.append("\n\tpublic void set" + name.substring(0, 1).toUpperCase()
					+ name.substring(1) + "(" + customs.get(name) + " " + name
					+ ")\n");
			out.append("\t{\n");
			out.append("\t\tthis." + name + " = " + name + ";\n");
			out.append("\t}");
		}
		return out.toString();
	}
	
	private static String generateInitializeCustomAttributes(
			TreeMap<String, String> customs)
	{
		StringBuilder out = new StringBuilder(1000);
		for (String name : customs.keySet())
		{
			out.append("\n\t\t\tthis.set" + name.substring(0, 1).toUpperCase()
					+ name.substring(1) + "(thisObject.get" + name.substring(0, 1).toUpperCase()
					+ name.substring(1)	+ "());");
		}
		return out.toString();
	}

	private static void generateJavaClass(String path, String clazz)
			throws IOException
	{
		String newFile = clazz.replace("Protocol", parameters.get(PROTOCOL_NAME));
		generateFromTemplate(
				path + "/" + newFile + ".java",
				"templates/" + clazz + ".java");
	}

	private static void write(PrintStream out, String string)
	{
		out.print(string);
	}
}
