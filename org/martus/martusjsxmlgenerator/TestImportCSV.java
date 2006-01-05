/*
 * Copyright 2005, The Benetech Initiative
 * 
 * This file is confidential and proprietary
 */
package org.martus.martusjsxmlgenerator;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class TestImportCSV extends TestCaseEnhanced 
{
	public TestImportCSV(String name) 
	{
		super(name);
	}

	protected void setUp() throws Exception 
	{
		super.setUp();
	}

	protected void tearDown() throws Exception 
	{
		super.tearDown();
	}
	
	public void testIncorrectDelimeter() throws Exception
	{
		File testJSFile = createTempFileFromName("$$$MARTUS_JS_TestFile_TabbedHeader");
		copyResourceFileToLocalFile(testJSFile, "test.js");
		File testCSVFile = createTempFileFromName("$$$MARTUS_CSV_TestFile_TabbedHeader");
		copyResourceFileToLocalFile(testCSVFile, "testTabHeaders.csv");
		try 
		{
			String INCORRECT_DELIMETER = ",";
			new ImportCSV(testJSFile, testCSVFile, INCORRECT_DELIMETER);
			fail("Should have thrown since the delimeter is incorrect");
		} 
		catch (Exception expected) 
		{
			assertContains("Only Found one column, please check your delimeter", expected.getMessage());
		}
		testCSVFile.delete();
		testJSFile.delete();
	}

	public void testGetTabbedHeaders() throws Exception
	{
		File testJSFile = createTempFileFromName("$$$MARTUS_JS_TestFile_TabbedHeader");
		copyResourceFileToLocalFile(testJSFile, "test.js");
		File testCSVFile = createTempFileFromName("$$$MARTUS_CSV_TestFile_TabbedHeader");
		copyResourceFileToLocalFile(testCSVFile, "testTabHeaders.csv");
		try 
		{
			new ImportCSV(testJSFile, testCSVFile, ",");
			fail("Should have thrown since the delimeter is incorrect");
		} 
		catch (Exception expected) 
		{
			assertContains("Only Found one column, please check your delimeter", expected.getMessage());
		}

		ImportCSV importer = new ImportCSV(testJSFile, testCSVFile, "\t");
		assertEquals(5, importer.headerLabels.length);
		testCSVFile.delete();
		testJSFile.delete();
	}

	public void testGetHeaders() throws Exception
	{
		File testJSFile = createTempFileFromName("$$$MARTUS_JS_TestFile_GetHeader");
		copyResourceFileToLocalFile(testJSFile, "test.js");
		File testCSVFile = createTempFileFromName("$$$MARTUS_CSV_TestFile_GetHeader");
		copyResourceFileToLocalFile(testCSVFile, "test.csv");
		ImportCSV importer = new ImportCSV(testJSFile, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
		String[] headerLabels = importer.headerLabels;
		assertEquals(9, headerLabels.length);
		assertEquals("language", headerLabels[0]);
		assertEquals("firstname", headerLabels[1]);
		assertEquals("lastname", headerLabels[2]);
		assertEquals("guns", headerLabels[8]);
		testCSVFile.delete();
		testJSFile.delete();
	}
	
	public void testHeaderCountDoesntMatchData() throws Exception
	{
		File testJSFile = createTempFileFromName("$$$MARTUS_JS_TestFile_HeaderCountDoesntMatchData");
		copyResourceFileToLocalFile(testJSFile, "test.js");
		File testInvalidCSVFile = createTempFileFromName("$$$MARTUS_CSV_TestFile_HeaderCountDoesntMatchData");
		copyResourceFileToLocalFile(testInvalidCSVFile, "testInvalidcolumncount.csv");
		ImportCSV importer = new ImportCSV(testJSFile, testInvalidCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
		try 
		{
			importer.doImport();
			fail("Should have thrown an exception");
		} 
		catch (Exception expected) 
		{
			assertContains("Row Data = en|John| Doe|Bulletin #1|Message 1|212|C.C.|no", expected.getMessage());
		}
		finally
		{
			testInvalidCSVFile.delete();
			testJSFile.delete();
		}
	}

	public void testStringFields() throws Exception
	{
		File testJSFile = createTempFileFromName("$$$MARTUS_JS_TestFile_StringFields");
		copyResourceFileToLocalFile(testJSFile, "test.js");
		File testCSVFile = createTempFileFromName("$$$MARTUS_CSV_TestFile_StringFields");
		copyResourceFileToLocalFile(testCSVFile, "test.csv");
		ImportCSV importer = new ImportCSV(testJSFile, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
		Context cs = Context.enter();
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "fr|Jane|Doe|16042001|Bulletin #2|Message 2|234|T.I..|yes";
		Scriptable fieldSpecs = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		
		MartusField field1 = (MartusField)fieldSpecs.get(0, scope);
		assertEquals("Author", field1.getTag());
		assertEquals("", field1.getLabel());
		assertEquals("Jane Doe", field1.getMartusValue(scope));

		MartusField field2 = (MartusField)fieldSpecs.get(1, scope);
		assertEquals("MyTitle", field2.getTag());
		assertEquals("My Title", field2.getLabel());
		assertEquals("Bulletin #2", field2.getMartusValue(scope));
		Context.exit();
		readerJSConfigurationFile.close();
		
		testCSVFile.delete();
		testJSFile.delete();
	}

	public void testType() throws Exception
	{
		File testJSFile = createTempFileFromName("$$$MARTUS_JS_TestFile_Type");
		copyResourceFileToLocalFile(testJSFile, "test.js");
		File testCSVFile = createTempFileFromName("$$$MARTUS_CSV_TestFile_Type");
		copyResourceFileToLocalFile(testCSVFile, "test.csv");
		ImportCSV importer = new ImportCSV(testJSFile, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
		Context cs = Context.enter();
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "fr|Jane|Doe|16042001|Bulletin #2|Message 2|234|T.I..|yes";
		Scriptable fieldSpecs = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		
		MartusField field1 = (MartusField)fieldSpecs.get(0, scope);
		assertEquals("STRING",field1.getType());
		Context.exit();
		readerJSConfigurationFile.close();
		
		testCSVFile.delete();
		testJSFile.delete();
	}
	
	public void testGetPrivateFieldSpec() throws Exception
	{
		ImportCSV importer = new ImportCSV();
		assertEquals(PRIVATE_FIELD_SPEC, importer.getPrivateFieldSpec());
	}
	
	public void testMartusFieldSpec() throws Exception
	{
		File testJSFile = createTempFileFromName("$$$MARTUS_JS_getMartusFieldSpec");
		copyResourceFileToLocalFile(testJSFile, "test.js");
		File testCSVFile = createTempFileFromName("$$$MARTUS_CSV_getMartusFieldSpec");
		copyResourceFileToLocalFile(testCSVFile, "test.csv");
		ImportCSV importer = new ImportCSV(testJSFile, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
		Context cs = Context.enter();
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "fr|Jane|Doe|16042001|Bulletin #2|Message 2|234|T.I..|yes";

		Scriptable bulletinData = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		ByteArrayOutputStream out = new ByteArrayOutputStream(2000);
		UnicodeWriter writer = new UnicodeWriter(out);
		importer.writeBulletinFieldSpecs(writer, scope, bulletinData);
		writer.close();
		out.close();
		assertEquals(MARTUS_PUBLIC_FIELD_SPEC + PRIVATE_FIELD_SPEC, out.toString());
		
		Context.exit();
		readerJSConfigurationFile.close();
		
		testCSVFile.delete();
		testJSFile.delete();
	}
	
	public void testMartusXMLValues() throws Exception
	{
		File testJSFile = createTempFileFromName("$$$MARTUS_JS_getMartusFieldSpec");
		copyResourceFileToLocalFile(testJSFile, "test.js");
		File testCSVFile = createTempFileFromName("$$$MARTUS_CSV_getMartusFieldSpec");
		copyResourceFileToLocalFile(testCSVFile, "test.csv");
		ImportCSV importer = new ImportCSV(testJSFile, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
		Context cs = Context.enter();
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "fr|Janice|Doe|16042001|Bulletin A|Message 2|234|T.I..|yes";

		Scriptable bulletinData = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		ByteArrayOutputStream out = new ByteArrayOutputStream(2000);
		UnicodeWriter writer = new UnicodeWriter(out);
		importer.writeBulletinFieldData(writer, scope, bulletinData);
		writer.close();
		out.close();
		assertEquals(MARTUS_XML_VALUES, out.toString());
		
		Context.exit();
		readerJSConfigurationFile.close();
		
		testCSVFile.delete();
		testJSFile.delete();
	}
	
	public void testImportMultipleBulletins()throws Exception
	{
		File testJSFile = createTempFileFromName("$$$MARTUS_JS_testImportMultipleBulletins");
		copyResourceFileToLocalFile(testJSFile, "test.js");
		File testCSVFile = createTempFileFromName("$$$MARTUS_CSV_testImportMultipleBulletins");
		copyResourceFileToLocalFile(testCSVFile, "test.csv");
		File testExpectedXMLFile = createTempFileFromName("$$$MARTUS_JS_testImportMultipleBulletins_EXPECTED");
		copyResourceFileToLocalFile(testExpectedXMLFile, "text_finalResult.xml");
		ImportCSV importer = new ImportCSV(testJSFile, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
		File xmlFile = importer.getXmlFile();
		xmlFile.deleteOnExit();
		try 
		{
			importer.doImport();
			UnicodeReader reader = new UnicodeReader(xmlFile);
			String data = reader.readAll();
			reader.close();
			
			UnicodeReader reader2 = new UnicodeReader(testExpectedXMLFile);
			String expectedData = reader2.readAll();
			reader2.close();
			
			assertEquals(expectedData,data);
		} 
		finally
		{
			testCSVFile.delete();
			testJSFile.delete();
			xmlFile.delete();
			testExpectedXMLFile.delete();
		}
		
	}
	
	
	
	
	public final String CSV_VERTICAL_BAR_REGEX_DELIMITER = "\\|";
	public final String PRIVATE_FIELD_SPEC = 
		    "<PrivateFieldSpecs>\n"+
			"<Field type='MULTILINE'>\n"+
			"<Tag>privateinfo</Tag>\n"+
			"<Label></Label>\n"+
			"</Field>\n"+
			"</PrivateFieldSpecs>\n\n";
	
	public final String MARTUS_PUBLIC_FIELD_SPEC =
		"<MartusBulletin>\n"+
		"<MainFieldSpecs>\n"+
		"<Field type='STRING'>\n"+
		"<Tag>Author</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='STRING'>\n"+
		"<Tag>MyTitle</Tag>\n"+
		"<Label>My Title</Label>\n"+
		"</Field>\n"+
		"</MainFieldSpecs>\n\n";
	
	public final String MARTUS_XML_VALUES =
		"<FieldValues>\n" +
		"<Field tag='Author'>\n" +
		"<Value>Janice Doe</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='MyTitle'>\n" +
		"<Value>Bulletin A</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='privateinfo'>\n" +
		"<Value>MY PRIVATE DATE = T.I..</Value>\n" +
		"</Field>\n\n" +
		"</FieldValues>\n"+
		"</MartusBulletin>\n\n";

}
