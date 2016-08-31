package org.tch.ft.manager.writers;

import java.io.PrintWriter;
import java.util.Set;

import org.hibernate.Session;
import org.tch.fc.model.VaccineGroup;
import org.tch.ft.model.TestPanel;

public interface TestCaseWriter
{
  public static enum FormatType {CDC, EPIC}
  
  public void setDataSession(Session dataSession) ;

  public void setCategoryNameSet(Set<String> categoryNameSet) ;

  public void setTestPanel(TestPanel testPanel);
  
  public void setVaccineGroup(VaccineGroup vaccineGroup);


  public void write(PrintWriter out) ;
  
  public String createFilename();
}