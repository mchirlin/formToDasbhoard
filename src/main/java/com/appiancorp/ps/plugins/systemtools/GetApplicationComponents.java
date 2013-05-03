package com.appiancorp.ps.plugins.systemtools;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.knowledge.Document;
import com.appiancorp.suiteapi.knowledge.DocumentDataType;
import com.appiancorp.suiteapi.process.ProcessExecutionService;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.MessageContainer;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.framework.SmartServiceContext;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;
import com.appiancorp.suiteapi.type.TypeService;

@PaletteInfo(paletteCategory="Appian Smart Services", palette="System Tools")
public class GetApplicationComponents extends AppianSmartService {

  private static final Logger LOG = Logger.getLogger(GetApplicationComponents.class);

  private final SmartServiceContext smartServiceCtx;
  private final ContentService cs;
  private final TypeService ts;
  private final ProcessExecutionService pes;
  private Long applicationZip;
  private Component[] components;

  private String errorMessage;

  @Override
  public void run() throws SmartServiceException {
      Document d = null;
      try {
	      d = (Document) cs.download(applicationZip, ContentConstants.VERSION_CURRENT, false)[0];
	      ZipFile zipFile = new ZipFile(d.getInternalFilename());
	      ZipEntry zipEntry = zipFile.getEntry("META-INF/export.log");

	      InputStream inputStream = zipFile.getInputStream(zipEntry);

	      String s = "";
	      byte[] buffer = new byte[1024];

	      while (inputStream.read(buffer, 0, buffer.length) != -1) {
	    	  s = s + new String(buffer).trim();
	      }

	      String compRegExp = "(\\w+) (\\d+) ([A-z0-9_-]+) \"([A-z\\s]+)\"";

	      Pattern p = Pattern.compile(compRegExp);
	      Matcher m = p.matcher(s);

	      List<Component> componentList = new ArrayList<Component>();

	      while (m.find()) {
	    	  Component comp = new Component(m.group(4), m.group(1), m.group(3));
	    	  componentList.add(comp);
	      }
	      components = new Component[componentList.size()];
	      components = componentList.toArray(components);
      } catch (Exception e) {
        errorMessage = "error.generic";
        throw createException(e, errorMessage);
      }
  }

  public GetApplicationComponents(SmartServiceContext smartServiceCtx, ContentService cs, TypeService ts, ProcessExecutionService pes) {
    super();
    this.smartServiceCtx = smartServiceCtx;
    this.cs = cs;
    this.ts = ts;
    this.pes = pes;
  }

  @Override
  public void onSave(MessageContainer messages) {
  }

  @Override
  public void validate(MessageContainer messages) {
  }

  @Input(required = Required.ALWAYS)
  @Name("ApplicationZip")
  @DocumentDataType
  public void setApplicationZip(Long val) {
    this.applicationZip = val;
  }

  @Name("Components")
  public Component[] getComponents() {
    return components;
  }

  private SmartServiceException createException(Throwable t, String key, Object... args) {
    return new SmartServiceException.Builder(getClass(), t).userMessage(key, args).build();
  }
}
