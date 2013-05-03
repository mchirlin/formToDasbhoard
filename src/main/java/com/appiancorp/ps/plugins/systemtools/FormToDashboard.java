package com.appiancorp.ps.plugins.systemtools;

import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.knowledge.Document;
import com.appiancorp.suiteapi.knowledge.DocumentDataType;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.MessageContainer;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.framework.SmartServiceContext;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@PaletteInfo(paletteCategory="Appian Smart Services", palette="System Tools")
public class FormToDashboard extends AppianSmartService {

	private static final Logger LOG = Logger.getLogger(FormToDashboard.class);

	private final ContentService cs;
	//Inputs
	private Long formFile;
	private String dashboardUIExpression;

	private String errorMessage;

	public FormToDashboard(SmartServiceContext smartServiceCtx, ContentService cs) {
		super();
		this.cs = cs;
	}

	@Override
	public void run() throws SmartServiceException {
		Document d = null;
		try {
			d = (Document) cs.download(formFile, ContentConstants.VERSION_CURRENT, false)[0];
			File file = new File(d.getInternalFilename());
			FileInputStream inputStream;

			inputStream = new FileInputStream(file);
			String s = IOUtils.toString(inputStream, "UTF-8");

			// Opening Column Array
			dashboardUIExpression =
				"=type!ColumnArrayLayout(" + "\n" +
				"  columns: {" + "\n";

			JsonParser jpar = new JsonParser();

			JsonObject jobj = jpar.parse(s).getAsJsonObject();

			JsonArray children = (JsonArray) jobj.getAsJsonObject("tree").getAsJsonArray("children");

			for (int i = 0; i < children.size(); i++) {
				JsonArray column = (JsonArray) children.get(i);

				// Opening Column
				dashboardUIExpression += 
					"    type!ColumnLayout(" + "\n" +
					"      contents: {" + "\n";

				String indent = "        ";
				for (int j = 0; j < column.size(); j++) {
					parseElement(indent, column.get(j), j, column.size() - 1, jobj);
				}

				// Closing Column
				dashboardUIExpression += 
					"      }" + "\n" +
					"    )";

				if (i < children.size() - 1) {
					this.dashboardUIExpression += ",\n";
				} else {
					this.dashboardUIExpression += "\n";
				}
			}

			// Closing Column Array
			dashboardUIExpression += 
				"  }" + "\n" +
				")" + "\n";

		} catch (Exception e) {
			errorMessage = "error.generic";
			throw createException(e, errorMessage);
		}
	}

	@Override
	public void onSave(MessageContainer messages) {
	}

	@Override
	public void validate(MessageContainer messages) {
	}

	private void parseChildren(String indent, JsonElement jel, JsonObject jobj) {
		JsonArray children = (JsonArray) jel.getAsJsonObject().getAsJsonArray("children");

		JsonObject job = jobj
		.getAsJsonObject("data")
		.getAsJsonObject(jel.getAsJsonObject().getAsJsonPrimitive("key").getAsString());
		
		String label;
		if (job.get("showLabel").getAsString().equals("no")) {
			label = "\"\"";
		} else {
			label = parseProperty(job, "label");
		}

		// Opening Section
		dashboardUIExpression += 
			indent + "type!SectionLayout(" + "\n" +
			indent + "  label: " + label + "," + "\n" +
			indent + "  content: type!ColumnArrayLayout(" + "\n" +
			indent + "    columns: {" + "\n";

		for (int i = 0; i < children.size(); i++) {
			JsonArray column = (JsonArray) children.get(i);

			// Opening Column
			dashboardUIExpression += 
				indent + "      type!ColumnLayout(" + "\n" +
				indent + "        contents: {" + "\n";

			for (int j = 0; j < column.size(); j++) {
				parseElement(indent + "          ", column.get(j), j, column.size() - 1, jobj);
			}

			// Closing Column
			dashboardUIExpression += 
				indent + "        }" + "\n" +
				indent + "      )";

			if (i < children.size() - 1) {
				this.dashboardUIExpression += ",\n";
			} else {
				this.dashboardUIExpression += "\n";
			}
		}

		// Closing Section
		dashboardUIExpression += 
			indent + "    }" + "\n" +
			indent + "  )" + "\n" +
			indent + ")";
	}

	private void parseElement(String indent, JsonElement jel, int index, int total, JsonObject jobj) {
		if (jel.isJsonObject()) {
			parseChildren(indent, jel, jobj);
		} else {
			JsonObject job = jobj.getAsJsonObject("data").getAsJsonObject(jel.getAsString());
			String typeName = job.get("typeName").getAsString();
			String label = parseProperty(job, "label");
			String instructions = parseProperty(job, "instructions");
			String value = parseProperty(job, "defaultValue");

			if (typeName.equals("text")) { 							// If it is a text field
				this.dashboardUIExpression += 
					indent + "type!TextField(" + "\n" +
					indent + "  label: " + label + "," + "\n" +
					indent + "  instructions: " + instructions + "," + "\n" +
					indent + "  readOnly: true()," + "\n" +
					indent + "  value: " + value + "\n" +
					indent + ")";
			} else if (typeName.equals("paragraph")) { 				// If it is a paragraph field
				this.dashboardUIExpression += 
					indent + "type!ParagraphField(" + "\n" +
					indent + "  label: " + label + "," + "\n" +
					indent + "  instructions: " + instructions + "," + "\n" +
					indent + "  readOnly: true()," + "\n" +
					indent + "  value: " + value + "\n" +
					indent + ")"; 
			} else if (typeName.equals("datetime")) { 				// If it is a date/datetime field
				if (job.get("subType").getAsString().equals("datetime")) {
					this.dashboardUIExpression += 
						indent + "type!DateTimeField(" + "\n" +
						indent + "  label: " + label + "," + "\n" +
						indent + "  instructions: " + instructions + "," + "\n" +
						indent + "  readOnly: true()," + "\n" +
						indent + "  value: " + value + "\n" +
						indent + ")";
				} else if (job.get("subType").getAsString().equals("date")) {
					this.dashboardUIExpression += 
						indent + "type!DateField(" + "\n" +
						indent + "  label: " + label + "," + "\n" +
						indent + "  instructions: " + instructions + "," + "\n" +
						indent + "  readOnly: true()," + "\n" +
						indent + "  value: " + value + "\n" +
						indent + ")";
				} else if (job.get("subType").getAsString().equals("time")) {
					this.dashboardUIExpression += 
						indent + "type!TimeField(" + "\n" +
						indent + "  label: " + label + "," + "\n" +
						indent + "  instructions: " + instructions + "," + "\n" +
						indent + "  readOnly: true()," + "\n" +
						indent + "  value: " + value + "\n" +
						indent + ")";
				} else {
					return;
				}
			} else if (typeName.equals("number")) { 				// If it is a integer/decimal field
				if (job.get("subType").getAsString().equals("integer")) {
					this.dashboardUIExpression += 
						indent + "type!IntegerField(" + "\n" +
						indent + "  label: " + label + "," + "\n" +
						indent + "  instructions: " + instructions + "," + "\n" +
						indent + "  readOnly: true()," + "\n" +
						indent + "  value: " + value + "\n" +
						indent + ")";
				}  else if (job.get("subType").getAsString().equals("decimal")) {
					this.dashboardUIExpression += 
						indent + "type!FloatingPointField(" + "\n" +
						indent + "  label: " + label + "," + "\n" +
						indent + "  instructions: " + instructions + "," + "\n" +
						indent + "  readOnly: true()," + "\n" +
						indent + "  value: " + value + "\n" +
						indent + ")";
				} else {
					return;
				}
			} else if (typeName.equals("link")) { 				// If it is a link field
				this.dashboardUIExpression += 
					indent + "type!LinkField(" + "\n" +
					indent + "  label: " + label + "," + "\n" +
					indent + "  links: type!SafeLink(" + "\n" +
					indent + "    uri: " + parseProperty(job, "defaultUrlValue") + "," + "\n" +
					indent + "    label: " + parseProperty(job, "defaultTitleValue") + "\n" +
					indent + "  )" + "\n" +
					indent + ")";
			} else if (typeName.equals("image")) { 				// If it is a image field
				this.dashboardUIExpression += 
					indent + "type!ImageField(" + "\n" +
					indent + "  label: " + label + "," + "\n" +
					indent + "  instructions: " + instructions + "," + "\n" +
					indent + "  source: " + parseProperty(job, "srcURL") + "," + "\n" +
					indent + "  altText: " + parseProperty(job, "alt") + "\n" +
					indent + ")";
			} else {
				return;
			}
		}
		if (index < total) {
			this.dashboardUIExpression += ",\n";
		} else {
			this.dashboardUIExpression += "\n";
		}
	}

	private String parseProperty(JsonObject job, String prop) {
		JsonElement ele = job.get(prop);
		if (ele != null) {
			String s = ele.getAsString();
			if (s.startsWith("=")) {
				return s.replace("pv", "rf").replace("=", "");	
			} else {
				return "\"" + s + "\"";
			}	
		} else {
			return "\"\"";
		}
	}

	@Input(required = Required.ALWAYS)
	@Name("formFile")
	@DocumentDataType
	public void setFormFile(Long formFile) {
		this.formFile = formFile;
	}

	@Name("DashboardUIExpression")
	public String getDashboardUIExpression() {
		return dashboardUIExpression;
	}

	private SmartServiceException createException(Throwable t, String key, Object... args) {
		return new SmartServiceException.Builder(getClass(), t).userMessage(key, args).build();
	}
}
